# 검증된 커뮤니티 모듈 사용 - VPC를 처음부터 손으로 짜지 않고, 서브넷/라우팅/NAT까지
# 표준적으로 구성해주는 모듈에 맡김 (실무에서도 VPC를 매번 새로 작성하는 경우는 드묾)
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "${var.project_name}-vpc"
  cidr = "10.0.0.0/16"

  # EKS는 최소 2개 가용영역(AZ)에 걸친 서브넷을 요구함
  azs             = ["${var.aws_region}a", "${var.aws_region}c"]
  private_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  public_subnets  = ["10.0.101.0/24", "10.0.102.0/24"]

  # private 서브넷(EKS 노드, RDS, ElastiCache)이 인터넷으로 나갈 때 씀
  # (EKS 노드가 Docker Hub에서 이미지를 pull하려면 필요함)
  enable_nat_gateway = true
  single_nat_gateway = true # AZ마다 하나씩 만들면 비용 2배 - 데모 목적이라 1개로 절약

  enable_dns_hostnames = true
  enable_dns_support   = true

  # EKS가 어떤 서브넷을 로드밸런서/노드용으로 쓸지 자동으로 찾을 수 있게 하는 필수 태그
  public_subnet_tags = {
    "kubernetes.io/cluster/${local.cluster_name}" = "shared"
    "kubernetes.io/role/elb"                      = "1"
  }
  private_subnet_tags = {
    "kubernetes.io/cluster/${local.cluster_name}" = "shared"
    "kubernetes.io/role/internal-elb"             = "1"
  }

  tags = {
    Project = var.project_name
  }
}
