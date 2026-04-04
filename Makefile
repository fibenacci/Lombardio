# Lombardio - Development & Infrastructure Makefile

.PHONY: help up down reset debug logs ps stats tf-plan tf-apply tf-init test fix

# --- Settings ---
SCRIPTS_DIR := scripts
DEV_SCRIPT := $(SCRIPTS_DIR)/dev.sh
TF_SCRIPT := $(SCRIPTS_DIR)/tf.sh

help: ## Show this help menu
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# --- Local Development (Docker Compose) ---

up: ## Start the full local stack (all profiles)
	@./$(DEV_SCRIPT) up all

lean: ## Start the lean local stack (no optional profiles)
	@./$(DEV_SCRIPT) up lean

debug: ## Start in Java Debug mode (attaches JDWP on ports 5005-5011)
	@./$(DEV_SCRIPT) debug all

down: ## Stop all local containers
	@./$(DEV_SCRIPT) down

reset: ## Stop containers and remove named volumes (DB reset)
	@./$(DEV_SCRIPT) reset

logs: ## Tail all service logs
	@./$(DEV_SCRIPT) logs

ps: ## Show container status
	@./$(DEV_SCRIPT) ps

stats: ## Show one-shot container CPU and memory usage
	@./$(DEV_SCRIPT) stats

# --- Infrastructure (Terraform) ---

tf-init: ## Initialize Terraform (env=local|aws|gcp)
	@./$(TF_SCRIPT) $(env) init

tf-plan: ## Plan Terraform changes (env=local|aws|gcp)
	@./$(TF_SCRIPT) $(env) plan

tf-apply: ## Apply Terraform changes (env=local|aws|gcp)
	@./$(TF_SCRIPT) $(env) apply

# --- Testing & Code Quality ---

test: ## Run all tests (Maven)
	mvn clean install

static: ## Run static code analysis (SpotBugs)
	./mvnw compile spotbugs:check

fix: ## Automatically fix code formatting (Java, Go, Frontend)
	@echo "🎨 Fixing Java (Spotless)..."
	./mvnw spotless:apply
	@echo "🐹 Fixing Go (gofmt)..."
	find services -name go.mod -execdir go fmt ./... \;
	@echo "⚛️ Fixing Frontend (eslint/prettier)..."
	cd frontend/app && npm run lint:fix --if-present
