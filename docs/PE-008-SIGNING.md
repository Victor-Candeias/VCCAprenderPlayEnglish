# PE-008 — Assinatura e verificação de certificados

A VCCAprender **só descobre plugins cujo certificado de assinatura é igual
ao do host**. A permissão `com.vcc.aprender.permission.PLAY_GAME` é do tipo
`signature` e o host efetua `PackageManager.checkSignatures` antes de apresentar
um jogo.

## Release — mesma keystore e alias do host

O `app/build.gradle.kts` do plugin segue o formato da aplicação principal,
com keystore **PKCS12**, e procura os seguintes valores (primeiro variáveis de
ambiente, depois o ficheiro local ignorado `keystore.properties`):

| Variável | Propriedade local | Descrição |
|---|---|---|
| `VCC_RELEASE_STORE_FILE` | `storeFile` | Caminho para o mesmo `VCCProjects.jks` do host |
| `VCC_RELEASE_STORE_PASSWORD` | `storePassword` | Password da keystore |
| `VCC_RELEASE_KEY_ALIAS` | `keyAlias` | Alias do certificado (padrão `vccprojects`) |
| `VCC_RELEASE_KEY_PASSWORD` | `keyPassword` | Password da chave (caso diferente da store) |

Por compatibilidade com o host, o caminho por omissão é
`~/.keystores/VCCProjects.jks`. A Release **falha** se a keystore ou
credenciais não estiverem disponíveis: não existe fallback para APK sem
assinatura ou para um certificado gerado automaticamente.

Exemplo de conteúdo do ficheiro **local não versionado**
`keystore.properties` (substituir apenas no computador seguro):

```properties
storeFile=/caminho/privado/VCCProjects.jks
storePassword=SUBSTITUIR_LOCALMENTE
keyAlias=vccprojects
keyPassword=SUBSTITUIR_LOCALMENTE
```

Compilação local:

```bash
./gradlew :app:assembleRelease
```

A **PE-010** disponibiliza a geração de Release assinada com versionamento
automático no GitHub Actions. Consultar [PE-010-RELEASE.md](PE-010-RELEASE.md).
Não foram adicionadas keystores ou passwords ao repositório.

## Debug — assinatura partilhada com o host

**Caso mais simples:** compilar o host e o plugin na **mesma máquina e no mesmo
perfil de utilizador**, usando o keystore Debug padrão do Android em ambos.
Esse certificado local difere da assinatura Debug criada pelo runner do GitHub
Actions. Descarregar dois APKs Debug produzidos em runners diferentes **não**
garante que os certificados coincidam.

Se necessário, o plugin suporta a sobreposição Debug por variáveis locais:

- `VCC_DEBUG_STORE_FILE` (caminho para a keystore partilhada)
- `VCC_DEBUG_STORE_PASSWORD`
- `VCC_DEBUG_KEY_ALIAS`
- `VCC_DEBUG_KEY_PASSWORD` (opcional; assume a password da store)

Se uma das variáveis Debug estiver definida, as três primeiras são
obrigatórias; configuração incompleta falha antes de compilar. **O host deve
usar precisamente o mesmo certificado** (independentemente do build type).
Nunca enviar chaves privadas por mensagem, issue, artefacto ou commit.

## Verificação com apksigner

No terminal com Android SDK instalado:

```bash
apksigner verify --print-certs VCCAprender-release.apk
apksigner verify --print-certs VCCAprenderPlayEnglish-release.apk
bash scripts/compare-apk-certificates.sh VCCAprender-release.apk VCCAprenderPlayEnglish-release.apk
```

O script falha se qualquer APK não estiver corretamente assinado, tiver
certificados de assinatura diferentes ou apresentar uma impressão SHA-256
incompatível. A mesma impressão pode surgir várias vezes quando o APK contém
vários esquemas de assinatura (v1/v2/v3).

A VCCAprender publicou uma verificação anterior da sua assinatura Release em
[`.verification/release-build2-signature.txt`](https://github.com/Victor-Candeias/VCCAprender/blob/main/.verification/release-build2-signature.txt),
com a impressão pública SHA-256:

```text
a334f539e112ef9c2fad14d47ed9762a69f6fb4165a47cefc678da47b700848b
```

Para confrontar o plugin Release **com esse certificado de referência**, sem
precisar do APK host local:

```bash
bash scripts/compare-apk-certificates.sh \
  --expected-sha256 a334f539e112ef9c2fad14d47ed9762a69f6fb4165a47cefc678da47b700848b \
  app/build/outputs/apk/release/app-release.apk
```

Esta impressão digital é pública e **não** é uma password, nem substitui a
keystore privada. Confirma sempre se a versão do host instalada continua a
usar o mesmo certificado.

## CI Debug e aceitação

O pipeline Debug da PE-009 continua sem acesso a secrets Release.
Valida criptograficamente o APK Debug produzido, testa o comparador contra
o próprio APK e verifica que um ficheiro inválido é recusado. Não afirma que
a assinatura do runner coincide com um host instalado.

**Critérios E2E (em dispositivo Android):**

1. Instalar VCCAprender e plugin assinados com o mesmo certificado; abrir o
   host e confirmar descoberta do jogo «VCC Brincar ao Inglês».
2. Reinstalar o plugin com um APK assinado por certificado diferente,
   garantindo que o host deixa de o reconhecer (desinstalar antes se o Android
   impedir a substituição pela diferença de certificado).
3. Confirmar que não existem keystores, passwords ou outros secrets nos
   ficheiros versionados, logs ou artefactos.

Esses testes E2E não são executados na CI Debug: dependem do host, de dois
certificados e de um emulador ou dispositivo.
