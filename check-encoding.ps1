# Check Java file encoding issues
# Usage: .\check-encoding.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Java File Encoding Checker" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$javaFiles = Get-ChildItem -Path . -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
$bomFiles = @()
$totalFiles = $javaFiles.Count
$checkedCount = 0

Write-Host "Scanning $totalFiles Java files..." -ForegroundColor Yellow
Write-Host ""

foreach ($file in $javaFiles) {
    $checkedCount++
    if ($checkedCount % 50 -eq 0) {
        Write-Host "Checked $checkedCount / $totalFiles files..." -ForegroundColor Gray
    }
    
    try {
        $bytes = [System.IO.File]::ReadAllBytes($file)
        
        # Check for BOM
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            $relativePath = $file.Replace($PWD.Path + "\", "")
            $bomFiles += $relativePath
        }
    }
    catch {
        Write-Host "Failed to read: $file" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Results" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($bomFiles.Count -eq 0) {
    Write-Host "SUCCESS: No BOM files found! All files are properly encoded." -ForegroundColor Green
} else {
    Write-Host "WARNING: Found $($bomFiles.Count) files with BOM:" -ForegroundColor Red
    Write-Host ""
    foreach ($file in $bomFiles) {
        Write-Host "  - $file" -ForegroundColor Yellow
    }
    Write-Host ""
    Write-Host "Run the following command to fix:" -ForegroundColor Cyan
    Write-Host ".\fix-bom.ps1" -ForegroundColor White
}

Write-Host ""
Write-Host "Total files checked: $totalFiles" -ForegroundColor Cyan
Write-Host "Files with BOM: $($bomFiles.Count)" -ForegroundColor $(if ($bomFiles.Count -eq 0) { 'Green' } else { 'Red' })
