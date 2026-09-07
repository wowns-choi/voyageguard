# RDS가 어느 서브넷들에 걸쳐 존재할 수 있는지 알려주는 그룹 - private 서브넷만 지정해서
# DB가 인터넷에 직접 노출되는 경로 자체를 차단
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = module.vpc.private_subnets

  tags = {
    Project = var.project_name
  }
}

# EKS 노드에서 오는 트래픽만 3306(MySQL) 포트로 허용 - 이게 없으면 RDS를 만들어도
# 앱이 아예 연결 자체를 못 함 (지난번 설명한 "제일 많이 놓치는 부분")
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-rds-sg"
  description = "Allow MySQL access from EKS nodes only"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "MySQL from EKS nodes"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [module.eks.node_security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Project = var.project_name
  }
}

# 물리 DB 분리 원칙(BC마다 독립 DB) 그대로 유지 - 4번 반복해서 쓰는 대신 for_each로
locals {
  bounded_contexts = ["planning", "sales", "payment", "auth"]
}

resource "aws_db_instance" "this" {
  for_each = toset(local.bounded_contexts)

  identifier     = "${var.project_name}-${each.key}"
  engine         = "mysql"
  engine_version = "8.0"
  instance_class = "db.t4g.micro" # 데모 목적 - 가장 저렴한 축의 인스턴스

  allocated_storage = 20 # RDS 최소 허용치
  storage_type      = "gp3"

  db_name  = "voyageguard_${each.key}"
  username = var.db_username
  password = var.db_password

  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  multi_az               = false # 비용 2배 방지 - 데모라 이중화 불필요

  # 데모용 - 삭제 시 최종 스냅샷 만드느라 destroy가 멈추는 걸 방지.
  # 실제 운영 DB라면 반드시 false로 바꿔서 삭제 전 스냅샷을 남겨야 함
  skip_final_snapshot = true

  tags = {
    Project        = var.project_name
    BoundedContext = each.key
  }
}
