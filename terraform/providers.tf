terraform {
  required_version = ">= 1.5"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

# 자격증명은 여기 안 넣음 - AWS CLI에서 `aws configure`로 미리 로그인해두면
# Terraform이 그 설정을 자동으로 읽어감 (환경변수/파일에 직접 키 박아두지 않기 위함)
provider "aws" {
  region = var.aws_region
}
