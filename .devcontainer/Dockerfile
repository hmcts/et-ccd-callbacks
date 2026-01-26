FROM mcr.microsoft.com/devcontainers/base:ubuntu

# Prevent broken Yarn apt repo from breaking apt-get update
RUN rm -f /etc/apt/sources.list.d/yarn.list \
          /etc/apt/trusted.gpg.d/yarn.gpg \
          /usr/share/keyrings/yarn-archive-keyring.gpg || true

# Install packages at build time (no sudo needed at runtime)
RUN apt-get update && apt-get install -y \
    iptables \
    dnsutils \
    curl \
    wget \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Java 21 is installed via devcontainer features (ghcr.io/devcontainers/features/java)
# This provides Temurin JDK 21 with proper JAVA_HOME setup

# Copy firewall script
COPY setup-firewall.sh /usr/local/bin/setup-firewall.sh
RUN chmod +x /usr/local/bin/setup-firewall.sh

# Copy entrypoint
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
CMD ["sleep", "infinity"]
