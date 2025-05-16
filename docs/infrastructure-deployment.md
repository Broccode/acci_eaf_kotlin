# 11. Infrastructure and Deployment Overview
>
> This document is a granulated shard from the main "ACCI-EAF-Architecture.md" focusing on "Infrastructure and Deployment Overview".

This section outlines the infrastructure environment for which the ACCI EAF and its derived applications are designed, along with the strategy for their deployment.

* **Target Infrastructure Provider(s):**
  * On-premise Data Centers / Private Cloud environments.
  * **Virtual Machines (VMs) are provided and managed by the customer**, typically running on IBM Power Architecture (ppc64le). ACCI EAF deployment does not include VM provisioning or OS-level configuration beyond what is necessary for Docker runtime.
  * No dependency on public cloud provider services (e.g., AWS, Azure, GCP) or Kubernetes for the core EAF and its MVP applications.

* **Core Services Utilized (deployed via Docker Compose on a single customer VM):**
  * The entire application stack, including all necessary services, is designed to run orchestrated by Docker Compose on a single VM provided by the customer. This includes:
    * **PostgreSQL Server:** Runs as a Docker container, managed within the Docker Compose setup. Its data will be persisted using Docker volumes mapped to the host VM.
    * **ACCI EAF Applications:** Runnable applications like `eaf-controlplane-api` and `eaf-license-server`, as well as applications built by end-users based on the ACCI EAF, will be deployed as Docker containers managed by Docker Compose.
    * **Web Server / Reverse Proxy (Optional):** If required (e.g., for SSL termination, serving static content for the Control Plane UI, or as an API gateway), a web server like Nginx or Traefik would also run as a Docker container within the same Docker Compose setup.

* **Infrastructure Definition & Application Packaging:**
  * **Application Packaging:** **Docker** is used to create container images for all runnable EAF components and PostgreSQL. All Docker images are built specifically for the **ppc64le** architecture. Dockerfiles will be maintained within each application module\'s source code.
  * **Runtime Orchestration on VM:** **Docker Compose** is the primary tool for defining, orchestrating, and managing the lifecycle of the entire application stack (database, backend applications, web server) on the customer\'s VM. A master `docker-compose.yml` file will define all services, networks, volumes, and configurations.
  * **VM Provisioning & Configuration:** This is the **responsibility of the customer**. The ACCI EAF deployment package assumes a VM with a compatible Linux OS, Docker (and Docker Compose) installed and running, and sufficient resources. No tools like Ansible for VM configuration are provided or required by ACCI for the EAF deployment itself.
  * **Delivery Package (for Air-Gapped Environments):**
    * For customer deployments, which are always considered air-gapped and without access to public Docker registries, ACCI will provide a **TAR ball**.
    * This TAR ball will contain:
      * All required Docker images (exported using `docker save`).
      * The `docker-compose.yml` file defining the entire stack.
      * Necessary helper scripts (e.g., Bash or Python) for installation, updates, and basic management (start, stop, status) of the stack.
      * Liquibase migration scripts for the database.
      * Configuration template files.

* **Deployment Strategy:**
  * **Artifacts:** The CI/CD pipeline (GitHub Actions) will build, test, and package the ppc64le Docker images. The final "release artifact" for customers is the aforementioned TAR ball.
  * **CI/CD Tool:** **GitHub Actions** for Continuous Integration, automated testing, and assembling the deployment TAR ball.
  * **Deployment to Customer VM (Air-Gapped Manual Process):**
        1. Secure transfer of the versioned TAR ball to the customer\'s environment.
        2. The customer (or an ACCI engineer on-site) unpacks the TAR ball on the target VM.
        3. The provided installation script is executed. This script will typically:
            *Load Docker images into the local Docker daemon on the VM (using `docker load < image.tar`).
            * Configure environment-specific parameters (e.g., network settings, external service URLs if any, secrets – potentially via a `.env` file used by Docker Compose).
            *Run database schema migrations using Liquibase (this might be integrated into an application\'s startup script or run as a separate step by the install script before starting the main application stack).
            * Start the entire application stack using `docker-compose up -d` with the provided `docker-compose.yml`.
  * **Updates:** Updates follow a similar process: deliver a new TAR ball, stop the current stack, load new images, potentially run data/schema migrations, and restart the stack with the updated configuration/images.

* **Environments:**
  * **Development:** Local developer machines using Docker Compose to accurately replicate the single-VM production setup.
  * **Staging/QA:** A dedicated VM environment for integration testing, UAT, and performance testing, deployed using the same TAR ball and Docker Compose methodology as production.
  * **Production:** The customer\'s live VM, deployed and managed as described above.

* **Environment Promotion Strategy:**
  * Code is developed and tested. Upon successful validation, a release candidate TAR ball is built.
  * This TAR ball is first deployed to the **Staging/QA** environment for thorough testing.
  * After successful Staging validation and sign-off, the *identical* TAR ball is approved for **Production** deployment by the customer.

* **Rollback Strategy:**
  * **Application Stack Rollback:** In case of a faulty deployment, the primary rollback strategy is to:
        1. Stop and remove the current Docker Compose stack (`docker-compose down`).
        2. If a previous version\'s TAR ball and loaded images are still available on the VM (or can be re-transferred), use the scripts and `docker-compose.yml` from that previous version to restart the older, stable stack.
        3. Careful management of Docker image tags within the TAR ball (e.g., `image:tag_version_X`) and corresponding `docker-compose.yml` files is crucial.
  * **Database Rollback:** Liquibase supports rollback commands for schema changes. Data state rollbacks would typically require restoring from a database backup. Procedures for database backup are the customer\'s responsibility but can be advised by ACCI.
