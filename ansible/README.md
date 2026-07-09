# Provisioning the biobank server with Ansible

Converges a fresh Ubuntu VM (over SSH) into a running biobank server. Idempotent - safe to re-run.

## Prerequisites

- Ansible, ansible-lint, and passlib on your machine (`pip install --user ansible ansible-lint passlib`). passlib is required on the controller for the `password_hash` filter that sets the biobank operator account's password (Python 3.11+ dropped the stdlib `crypt` module Ansible used before).
- SSH access to the target VM as a sudo-capable user.
- The initial DB dump reachable at a URL.

## Configure

1. Edit `inventory/hosts.yml`: set `ansible_host` and `ansible_user` for the target.
2. Edit `group_vars/biobank/vars.yml`: set `biobank_cert_cn`, `biobank_cert_san`
   (must include the address the thick client uses), and `biobank_db_dump_url`.
3. Create the encrypted vault:
   ```sh
   cp group_vars/biobank/vault.example.yml group_vars/biobank/vault.yml
   # fill in vault_db_root_password / vault_db_password
   ansible-vault encrypt group_vars/biobank/vault.yml
   ```

## Run

```sh
cd ansible
ansible-playbook site.yml --ask-vault-pass --ask-become-pass
```

Re-run any time to converge. One-time work (DB dump fetch, cert generation,
image build) is skipped once done; a live database is never overwritten.

## Operate

The stack uses compose `restart: unless-stopped`, so containers return after a
reboot. Manage them from the repo on the VM with the existing helpers:
`./bb-start.sh`, `./bb-stop.sh`, `./bb-status.sh`.
