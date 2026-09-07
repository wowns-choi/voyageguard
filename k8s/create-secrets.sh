#!/bin/bash
# RDS/ElastiCache를 실제로 만든 뒤, 아래 <...> 부분을 실제 값으로 채워서 한 번씩 실행할 것.
# 이 스크립트 자체엔 진짜 시크릿이 없어서 커밋해도 안전함 - 실행은 로컬에서만, 커밋은 절대 금지.
#
# Secret을 YAML 파일로 만들어서 커밋하지 않는 이유: Secret의 값은 "base64 인코딩"일 뿐 암호화가
# 아니라서, 커밋되는 순간 평문 시크릿을 올리는 것과 사실상 같음. 그래서 kubectl 명령으로
# "그때그때 클러스터에 직접 주입"하는 방식(이 스크립트)만 쓰고, 결과물(Secret 객체)은 git에 안 남김.

kubectl create secret generic planning-db-secret \
  --from-literal=datasource-url='jdbc:mysql://<RDS_PLANNING_ENDPOINT>:3306/voyageguard_planning' \
  --from-literal=username='<RDS_USERNAME>' \
  --from-literal=password='<RDS_PASSWORD>'

kubectl create secret generic sales-db-secret \
  --from-literal=datasource-url='jdbc:mysql://<RDS_SALES_ENDPOINT>:3306/voyageguard_sales' \
  --from-literal=username='<RDS_USERNAME>' \
  --from-literal=password='<RDS_PASSWORD>'

kubectl create secret generic sales-redis-secret \
  --from-literal=host='<ELASTICACHE_ENDPOINT>'

kubectl create secret generic payment-db-secret \
  --from-literal=datasource-url='jdbc:mysql://<RDS_PAYMENT_ENDPOINT>:3306/voyageguard_payment' \
  --from-literal=username='<RDS_USERNAME>' \
  --from-literal=password='<RDS_PASSWORD>'

kubectl create secret generic payment-secrets \
  --from-literal=toss-secret-key='<TOSS_SECRET_KEY>'

kubectl create secret generic auth-db-secret \
  --from-literal=datasource-url='jdbc:mysql://<RDS_AUTH_ENDPOINT>:3306/voyageguard_auth' \
  --from-literal=username='<RDS_USERNAME>' \
  --from-literal=password='<RDS_PASSWORD>'

# 구글 로그인 실제로 안 쓸 거면 더미값으로 채워도 됨(docker-compose 때와 같은 이유 - 값이
# 비어있으면 OAuth2Client 빈 생성 자체가 실패해서 Auth가 기동을 못 함)
kubectl create secret generic auth-secrets \
  --from-literal=google-client-id='<GOOGLE_CLIENT_ID_또는_dummy-client-id>' \
  --from-literal=google-client-secret='<GOOGLE_CLIENT_SECRET_또는_dummy-client-secret>'
