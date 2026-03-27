#循环调用order/{id}接口
for ($i = 1; $i -le 100; $i++) { try { $r = Invoke-RestMethod -Uri "http://localhost:11000/order/1" -Method Get; Write-Host "$i Success" -ForegroundColor Green } catch { Write-Host "$i BLOCKED!" -ForegroundColor Red } Start-Sleep -Milliseconds 100 }
