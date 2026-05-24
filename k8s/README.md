# OmniEats on Kubernetes (Minikube)

Deploys the full stack to a local Kubernetes cluster, satisfying:

- **II.1 Config Centralizată** — `omnieats-config` ConfigMap + `omnieats-secrets` Secret (`01-config.yaml`, `02-secrets.yaml`).
- **II.2 Service Discovery** — every component is a ClusterIP `Service`; the apps reach each other by name via Kubernetes DNS (reusing the `docker` Spring profile, no code changes).
- **II.3 Load Balancing** — `restaurant-service` runs **3 replicas** behind one ClusterIP (`21-restaurant.yaml`).
- **Bonus deployment** — runs end-to-end on Minikube, monitoring included.

Everything lives in the `omnieats` namespace. No application/Dockerfile/compose changes were needed.

> **Secrets note:** the document mentions "AI API keys", but this project uses a **local Ollama** (no external key). The real secrets are the **JWT signing key** (identity-service signs, gateway-service validates — must match) and the **DB password**, both in `omnieats-secrets`.

## Prerequisites
`docker`, `minikube`, and `kubectl` installed.

## 1. Build the application images
The manifests use the images Docker Compose builds (project name `docker` → `docker-<service>`). From the repo's `docker/` directory:
```bash
cd docker
docker compose build          # builds docker-{gateway,identity,restaurant,ai-aggregator}-service + docker-frontend
```

## 2. Start Minikube and load the images
`llama3.1:8b` is ~5 GB, so give the VM room (the Resilience4j fallback means AI still works if RAM is tight / the model isn't pulled):
```bash
minikube start --memory=8192 --cpus=4
for img in gateway-service identity-service restaurant-service ai-aggregator-service; do
  minikube image load "docker-$img:latest"
done
minikube image load docker-frontend:latest
```
> **Kind instead of Minikube?** Manifests are identical; replace the load step with
> `kind load docker-image docker-<svc>:latest` for each image.

## 3. (Optional) Pre-load the Grafana dashboards
The dashboard JSONs are kept as files; turn them into the ConfigMap the Grafana pod mounts:
```bash
kubectl create configmap grafana-dashboards \
  --from-file=../docker/grafana/dashboards/ \
  -n omnieats --dry-run=client -o yaml | kubectl apply -f -
```
(Skipping this is fine — Grafana still starts with the Prometheus datasource wired; it just has no pre-built dashboards. The volume is marked `optional`.)

## 4. Deploy
```bash
kubectl apply -f k8s/
kubectl get pods -n omnieats -w     # wait until all are Running/Ready
```
Backends may `CrashLoopBackOff` briefly until postgres/redis are ready, then recover (Kubernetes has no `depends_on`; readiness probes keep half-started pods out of rotation).

## 5. (Optional) Pull the LLM model
```bash
kubectl exec -n omnieats deploy/ollama -- ollama pull llama3.1:8b
```
Without this, AI Search uses the keyword fallback.

## 6. Access the app
Keep these `port-forward`s running (the frontend's production build calls the API at `http://localhost:8080`, so the gateway must be on 8080):
```bash
kubectl port-forward -n omnieats svc/gateway-service 8080:8080 &
kubectl port-forward -n omnieats svc/frontend        3000:80   &
kubectl port-forward -n omnieats svc/grafana         3001:3000 &
kubectl port-forward -n omnieats svc/prometheus      9090:9090 &
```
Open <http://localhost:3000> and log in with the seeded `admin@omnieats.com` / `admin123`.

## Verify the requirements
```bash
# II.3 Load balancing — 3 restaurant pods behind one Service
kubectl get pods -l app=restaurant-service -n omnieats        # 3/3 Running
kubectl get endpoints restaurant-service -n omnieats          # 3 pod IPs

# II.2 Service discovery — logging in / browsing restaurants exercises
# gateway -> identity/restaurant resolved purely by Kubernetes DNS.

# II.1 Centralized config
kubectl get configmap,secret -n omnieats
kubectl describe pod -l app=restaurant-service -n omnieats | grep -A3 "Environment"

# Monitoring — Prometheus targets UP (localhost:9090/targets), Grafana dashboards (localhost:3001)
```

## Teardown
```bash
kubectl delete namespace omnieats
# and optionally: minikube stop   (or: minikube delete)
```

## Files
| File | Purpose |
|------|---------|
| `00-namespace.yaml` | `omnieats` namespace |
| `01-config.yaml` | `omnieats-config` + `postgres-init` ConfigMaps |
| `02-secrets.yaml` | `omnieats-secrets` (JWT + DB password) |
| `10-postgres.yaml` `11-redis.yaml` `12-ollama.yaml` | backing stores (postgres/ollama have PVCs) |
| `20-identity.yaml` `21-restaurant.yaml` `22-ai-aggregator.yaml` `23-gateway.yaml` | backend services (restaurant = 3 replicas) |
| `30-frontend.yaml` | nginx SPA |
| `40-prometheus.yaml` `41-grafana.yaml` | monitoring |
