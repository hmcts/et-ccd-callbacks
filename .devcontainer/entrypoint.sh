#!/bin/bash
# Entrypoint script - runs as root at container startup
# Sets up firewall before user session begins

# Run firewall setup
/usr/local/bin/setup-firewall.sh

# Execute the main command
exec "$@"
