# EBS CSI 드라이버가 AWS 리소스(EBS 볼륨)를 만들 권한을 가지려면 IAM 역할이 필요함 -
# IRSA(IAM Roles for Service Accounts)로 "이 K8s 서비스어카운트는 이 IAM 역할을 써라"고 연결
module "ebs_csi_irsa_role" {
  source  = "terraform-aws-modules/iam/aws//modules/iam-role-for-service-accounts-eks"
  version = "~> 5.0"

  role_name             = "${var.project_name}-ebs-csi"
  attach_ebs_csi_policy = true

  oidc_providers = {
    main = {
      provider_arn               = module.eks.oidc_provider_arn
      namespace_service_accounts = ["kube-system:ebs-csi-controller-sa"]
    }
  }
}

module "eks" {
  source  = "terraform-aws-modules/eks/aws"
  version = "~> 20.0"

  cluster_name    = local.cluster_name
  cluster_version = "1.30"

  # 데모용 - kubectl을 어디서든(집/회사) 바로 쓸 수 있게 API 서버를 공개로 둠.
  # 실제 운영이라면 private access + VPN/Bastion으로 막아야 함
  cluster_endpoint_public_access = true

  vpc_id     = module.vpc.vpc_id
  subnet_ids = module.vpc.private_subnets

  enable_irsa = true

  # Kafka PVC(EBS 볼륨)가 실제로 만들어지려면 이 애드온이 필수
  cluster_addons = {
    coredns    = { most_recent = true }
    kube-proxy = { most_recent = true }
    vpc-cni    = { most_recent = true }
    aws-ebs-csi-driver = {
      most_recent              = true
      service_account_role_arn = module.ebs_csi_irsa_role.iam_role_arn
    }
  }

  eks_managed_node_groups = {
    default = {
      # 앱 4개(각 100m~500m cpu, 256Mi~512Mi 메모리) + Kafka(250m~500m cpu, 512Mi~1Gi) +
      # 시스템 파드까지 감안한 사이징. HPA가 늘릴 여유(max_size)도 조금 둠
      instance_types = ["t3.medium"]
      min_size       = 1
      max_size       = 3
      desired_size   = 2
    }
  }

  tags = {
    Project = var.project_name
  }
}
