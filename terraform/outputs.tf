# apply 끝난 뒤 이 값들을 create-secrets.sh의 <...> 자리에 채워 넣을 것

output "rds_endpoints" {
  description = "BC별 RDS 접속 주소 (포트 제외, create-secrets.sh의 <RDS_XXX_ENDPOINT> 자리에 사용)"
  value       = { for bc, db in aws_db_instance.this : bc => db.address }
}

output "elasticache_endpoint" {
  description = "Redis 접속 주소 (create-secrets.sh의 <ELASTICACHE_ENDPOINT> 자리에 사용)"
  value       = aws_elasticache_cluster.main.cache_nodes[0].address
}

output "eks_cluster_name" {
  description = "EKS 클러스터 이름"
  value       = module.eks.cluster_name
}

# 이 출력값을 그대로 복붙해서 실행하면 kubectl이 이 클러스터를 바라보게 설정됨
output "configure_kubectl" {
  description = "kubectl이 이 EKS 클러스터를 바라보게 설정하는 명령어"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${module.eks.cluster_name}"
}
