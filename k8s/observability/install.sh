#!/bin/bash
# K8s 클러스터(EKS)에 PLG 스택을 Helm으로 설치. EKS 클러스터가 준비되고
# kubectl이 그 클러스터를 바라보게 설정된 뒤(terraform output의 configure_kubectl) 실행할 것.

set -e

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

# 로컬에서 이미 만들어둔 대시보드 JSON을 ConfigMap으로 등록 - prometheus-values.yaml의
# grafana.dashboardsConfigMaps가 이 이름을 참조함
kubectl create configmap voyageguard-dashboard \
  --from-file=voyageguard-overview.json=../../observability/grafana/provisioning/dashboards/voyageguard-overview.json

helm install loki grafana/loki-stack -f loki-values.yaml
helm install prometheus prometheus-community/kube-prometheus-stack -f prometheus-values.yaml

echo ""
echo "완료. Grafana 접속: kubectl port-forward svc/prometheus-grafana 3000:80 (admin/admin)"
echo ""
echo "TODO(수동 작업 필요): Slack 알람 규칙 4개(DB 커넥션 풀/서비스 다운/에러율/p95)는"
echo "로컬 Grafana에서 API로 만든 것이라 이 새 Grafana엔 없음 - 배포 후 동일하게 재등록할 것."
