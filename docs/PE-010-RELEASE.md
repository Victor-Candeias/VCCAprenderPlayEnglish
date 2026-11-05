# PE-010 — Releases assinadas e versionadas automaticamente

O plugin tem o workflow [Android Release - Signed APK](../.github/workflows/android-release.yml), que **executa automaticamente em cada push/merge para `main`** e também permite a execução manual (`workflow_dispatch`). É independente do pipeline Debug da PE-009 e nunca publica a partir de PRs/branches de desenvolvimento. Se falhar uma etapa, a execução apresenta erro e não substitui o APK Release por Debug.

## 1. Preparação de GitHub Secrets

Em **Settings → Secrets and variables → Actions**, criar os quatro secrets de
repositório (ou do ambiente GitHub `release`):

| Secret | Conteúdo |
|---|---|
| `VCC_RELEASE_KEYSTORE_BASE64` | Keystore PKCS12/JKS da **mesma assinatura da VCCAprender**, codificada em Base64 |
| `VCC_RELEASE_STORE_PASSWORD` | Palavra-passe da keystore |
| `VCC_RELEASE_KEY_ALIAS` | Alias real da chave (ex.: `vccprojects`) |
| `VCC_RELEASE_KEY_PASSWORD` | Palavra-passe da chave privada |

A codificação Base64 **não protege a chave**: o valor deve existir apenas em
GitHub Secrets, nunca no repositório, nos logs nem em artefactos. O workflow
reconstrói a keystore com permissão `0600` em `RUNNER_TEMP`, fora do
workspace e remove-a numa etapa `always()`.

O workflow utiliza o ambiente `release`. É recomendado configurar
*Required reviewers* e permitir apenas a branch `main` nesse ambiente
(essa proteção do ambiente requer configuração por um administrador).

Pode configurar-se ainda a variável GitHub não secreta
`VCC_HOST_RELEASE_SHA256` caso o host mude legitimamente de certificado.
Por omissão utiliza-se a impressão pública já documentada pela VCCAprender:

```text
a334f539e112ef9c2fad14d47ed9762a69f6fb4165a47cefc678da47b700848b
```

Um certificado do plugin diferente **bloqueia a Release** antes da publicação.

## 2. Executar a Release

1. Confirmar os quatro secrets e que a keystore é a mesma que assinou a VCCAprender instalada.
2. Integrar alterações em `main`: o GitHub Actions lança automaticamente o build Release assinado (em paralelo com o pipeline Debug). Não é necessário clicar em **Run workflow**.
3. Em **Actions → Android Release - Signed APK**, verificar que a execução está **Success**. Opcionalmente, iniciar uma Release extra com **Run workflow**, escolhendo `main`.
4. Descarregar o artefacto `VCC-Brincar-ao-Ingles-vX.Y.Z-release`, que contém exatamente:
   `VCC-Brincar-ao-Ingles-vX.Y.Z-release.apk`.

O nome do ficheiro corresponde ao nome apresentado pela aplicação, **VCC Brincar ao Inglês**, sem espaços nem acentos para facilitar o download e a instalação no Windows/Android.
O APK mantém `applicationId=com.vcc.aprender.playenglish` e o nome visível `VCC Brincar ao Inglês`; apenas o **nome do ficheiro** é adaptado.

**Importante:** se existir uma aprovação obrigatória no ambiente GitHub `release`, o job ficará pendente até essa aprovação. Os quatro secrets podem estar configurados ao nível do repositório ou do ambiente. Falhas de credenciais, certificado, `aapt`, upload ou criação da tag deixam o workflow a vermelho; não há APK Release não assinado nem fallback para Debug.

A primeira Release sem tags prévias será **v0.1.0**. A seguinte será
**v0.1.1**. O `scripts/release_version.py` usa as tags `vMAJOR.MINOR.PATCH`
existentes e incrementa o patch após uma publicação. Ao atingir 999,
incrementa a versão menor, mantendo um `versionCode` numérico sempre
crescente: `major*1000000 + minor*1000 + patch`.
As componentes minor/patch são limitadas a 999 e o `versionCode` ao máximo
2100000000. O script falha perante versões não suportadas.

A compilação configura `appVersion` e `buildNumber` diretamente no Gradle,
sem commits de alterações de versão. O workflow valida com `aapt`
o `applicationId`, `versionName` e `versionCode` efetivos.

## 3. Ordem transacional

1. Calcular a próxima versão a partir das tags publicadas.
2. Validar secrets; preparar keystore fora do repositório.
3. Validar o catálogo offline, executar testes JVM e compilar Release assinada.
4. Validar a assinatura com `apksigner`, comparar o SHA-256 ao host e verificar
   versão e package ID no APK real. Copiar para o ficheiro com o nome da app,
   verificando que os bytes são idênticos ao APK assinado.
5. Carregar o APK assinado nos **Artifacts** do GitHub Actions.
6. **Só depois do upload ter sido concluído com sucesso**, criar e enviar a tag
   anotada `vX.Y.Z`.

Se a build, a verificação ou o upload falhar, **não é criada uma tag**. Uma
nova execução volta a tentar a mesma versão. Se houver falha no envio da tag
depois do upload, o artefacto anterior continuará disponível mas a Release
não fica concluída; deve-se verificar a tag e repetir, sem substituir tags.
A secção `concurrency` impede duas Releases simultâneas no workflow.

## 4. Testes automatizados e limitações

O pipeline Debug executa `python3 -m unittest discover -s scripts
-p "test_release_version.py" -v` sem secrets. Estes testes abrangem a
primeira versão, ordenação SemVer, monotonicidade e limites do versionCode.
A validação real de Release assinada **não pode ser executada sem a keystore
privada legítima**.

O certificado pode coincidir e, ainda assim, ser necessário testar a
descoberta da Activity e o protocolo com a aplicação VCCAprender instalada
num dispositivo Android. Não são assumidos testes E2E concluídos só pelo
sucesso do build.

O pipeline não configura GitHub Releases nem publica automaticamente em
lojas de aplicações. A distribuição aqui prevista é via **Actions Artifacts**.
