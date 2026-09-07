resource "aws_elasticache_subnet_group" "main" {
  name       = "${var.project_name}-redis-subnet-group"
  subnet_ids = module.vpc.private_subnets
}

# RDS 때와 똑같은 패턴 - EKS 노드에서 오는 트래픽만 6379(Redis) 포트로 허용
resource "aws_security_group" "redis" {
  name        = "${var.project_name}-redis-sg"
  description = "Allow Redis access from EKS nodes only"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description     = "Redis from EKS nodes"
    from_port       = 6379
    to_port         = 6379
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

# 로컬 docker-compose의 redis:7 컨테이너(단일 노드, 복제 없음)와 동일한 수준으로 구성 -
# 데모 목적이라 복제/장애복구(replication group)까지는 불필요
resource "aws_elasticache_cluster" "main" {
  cluster_id           = "${var.project_name}-redis"
  engine               = "redis"
  node_type            = "cache.t3.micro"
  num_cache_nodes      = 1
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.main.name
  security_group_ids   = [aws_security_group.redis.id]

  tags = {
    Project = var.project_name
  }
}
