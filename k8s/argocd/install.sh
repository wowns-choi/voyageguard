#!/bin/bash
# EKS 클러스터에 ArgoCD를 Helm으로 설치하고, voyageguard Application을 등록.
# 클러스터가 준비되고 kubectl이 그 클러스터를 바라보게 설정된 뒤(terraform output의
# configure_kubectl) 실행할 것.

set -e

helm repo add argo https://argoproj.github.io/argo-helm
helm repo update

helm install argocd argo/argo-cd -n argocd --create-namespace

kubectl -n argocd rollout status deployment/argocd-server --timeout=180s

kubectl apply -f k8s/argocd/application.yaml

echo ""
echo "초기 admin 비밀번호:"
kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d
echo ""
echo ""
echo "접속: kubectl port-forward svc/argocd-server -n argocd 8080:443 (https://localhost:8080, admin/위 비밀번호)"
