variable "aws_region" {
  description = "리소스를 생성할 AWS 리전"
  type        = string
  default     = "ap-northeast-2" # 서울
}

variable "project_name" {
  description = "리소스 이름/태그에 붙일 프로젝트 식별자"
  type        = string
  default     = "voyageguard"
}

variable "db_username" {
  description = "RDS 마스터 계정 이름"
  type        = string
  default     = "admin"
}

# 기본값을 일부러 안 둠 - 커밋되는 파일에 비밀번호가 남는 걸 원천 차단하기 위함.
# 실행 시 `terraform apply -var="db_password=..."` 또는 TF_VAR_db_password 환경변수로 주입할 것
variable "db_password" {
  description = "RDS 마스터 계정 비밀번호"
  type        = string
  sensitive   = true
}
