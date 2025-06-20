unset POSTGRES_IP
POSTGRES_IP=$(kubectl get endpoints et-ccd-definitions-admin-pr-"$1"-postgresql -o jsonpath='{.subsets[0].addresses[0].ip}' -n et)
if [[ "$POSTGRES_IP" =~ ^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  psql postgresql://hmcts:hmcts@"$POSTGRES_IP"/et_cos -f ./bin/preview/sql/dev-ref-data.sql
  exit 0
else
  psql postgresql://hmcts:"$2"@et-preview.postgres.database.azure.com/pr-"$1"-et_cos -f ./bin/preview/sql/dev-ref-data.sql
  exit 0
fi
echo "Importing ref data to preview single and flexi db failed."
exit 0