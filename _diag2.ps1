$key='sk-smmmripqxjtpgyzskvxkhuqbikicogptcbojkuanxewdrpxq'
$body = @{model='BAAI/bge-large-zh-v1.5'; input='你告诉我这个医院的院长是谁？'} | ConvertTo-Json -Compress
$e = Invoke-RestMethod -Uri 'https://api.siliconflow.cn/v1/embeddings' -Method POST -ContentType 'application/json' -Headers @{'Authorization'="Bearer $key"} -Body $body
$vec = ($e.data | Sort-Object index | Select-Object -First 1).embedding
$cols = Invoke-RestMethod -Uri 'http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections'
$kb = $cols | Where-Object { $_.name -eq 'hospital_kb' }
Write-Host "COLLECTION_ID=$($kb.id)"
$cnt = Invoke-RestMethod -Uri "http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections/$($kb.id)"
Write-Host "COUNT=$($cnt.count)"
$q = @{query_embeddings=@(,$vec); n_results=3; include=@('documents','metadatas')} | ConvertTo-Json -Depth 5 -Compress
$url = "http://localhost:8000/api/v2/tenants/default_tenant/databases/default_database/collections/$($kb.id)/query"
$r = Invoke-RestMethod -Uri $url -Method POST -ContentType 'application/json' -Body $q
Write-Host "RAW_RESPONSE:"
$r | ConvertTo-Json -Depth 6
