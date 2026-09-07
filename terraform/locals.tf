# 여러 파일(vpc.tf, eks.tf 등)에서 똑같이 재사용할 값들 - 한 곳에서만 관리해서
# "VPC 태그의 클러스터 이름"과 "실제 EKS 클러스터 이름"이 어긋나는 실수를 방지
locals {
  cluster_name = "${var.project_name}-eks"
}
