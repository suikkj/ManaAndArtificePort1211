param(
    [string]$SourceDir = ".\src\main\java"
)

Write-Host "Iniciando refatoração em massa de imports (Forge -> NeoForge)..."

$files = Get-ChildItem -Path $SourceDir -Recurse -Filter "*.java"
$total = $files.Count
$count = 0

foreach ($file in $files) {
    $count++
    if ($count % 100 -eq 0) { Write-Host "Progresso: $count / $total" }
    
    $content = Get-Content $file.FullName -Raw
    $original = $content
    
    # Substituições básicas
    $content = $content -replace "net\.minecraftforge\.event\.", "net.neoforged.neoforge.event."
    $content = $content -replace "net\.minecraftforge\.eventbus\.api\.", "net.neoforged.bus.api."
    $content = $content -replace "net\.minecraftforge\.common\.", "net.neoforged.neoforge.common."
    $content = $content -replace "net\.minecraftforge\.fml\.", "net.neoforged.fml."
    $content = $content -replace "net\.minecraftforge\.network\.", "net.neoforged.neoforge.network."
    $content = $content -replace "net\.minecraftforge\.api\.distmarker\.", "net.neoforged.api.distmarker."
    $content = $content -replace "net\.minecraftforge\.client\.", "net.neoforged.neoforge.client."
    $content = $content -replace "net\.minecraftforge\.registries\.", "net.neoforged.neoforge.registries."
    
    if ($content -cne $original) {
        $content | Set-Content $file.FullName -Encoding UTF8
    }
}

Write-Host "Refatoração de pacotes concluída!"
