#!/bin/bash
# Firewall setup script for Agent Development Container
# This script blocks access to specific domains that agents should not access

set -e

echo "Setting up firewall rules for agent isolation..."

# Domains to block - add or remove as needed
BLOCKED_DOMAINS=(
    "facebook.com"
    "twitter.com"
    "x.com"
    "instagram.com"
    "tiktok.com"
    "reddit.com"
    "linkedin.com"
)

# Flush existing rules
iptables -F OUTPUT
iptables -F INPUT

# Allow loopback traffic
iptables -A OUTPUT -o lo -j ACCEPT
iptables -A INPUT -i lo -j ACCEPT

# Allow established and related connections
iptables -A OUTPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# Function to block a domain by resolving its IPs
block_domain() {
    local domain=$1
    echo "Blocking domain: $domain"

    # Resolve domain to IP addresses (may return multiple)
    local ips=$(dig +short "$domain" A 2>/dev/null | grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$')

    for ip in $ips; do
        iptables -A OUTPUT -d "$ip" -j DROP
        echo "  Blocked IP: $ip"
    done

    # Also block www subdomain
    local www_ips=$(dig +short "www.$domain" A 2>/dev/null | grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$')

    for ip in $www_ips; do
        iptables -A OUTPUT -d "$ip" -j DROP
        echo "  Blocked IP: $ip (www)"
    done
}

# Block each domain
echo ""
echo "Blocking configured domains..."
for domain in "${BLOCKED_DOMAINS[@]}"; do
    block_domain "$domain"
done

# Allow all other traffic (default allow policy)
iptables -A OUTPUT -j ACCEPT

echo ""
echo "Firewall rules applied successfully."
echo "Blocked domains: ${BLOCKED_DOMAINS[*]}"
echo "All other outbound traffic is allowed."
