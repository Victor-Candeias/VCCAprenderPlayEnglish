# 🇬🇧 VCC Brincar ao Inglês

### 🌟 Aprender inglês nunca foi tão divertido!

**VCC Brincar ao Inglês** é uma aplicação educativa para Android, desenvolvida como plugin da **VCCAprender**, destinada a crianças dos **3 aos 8 anos**. O objetivo é ensinar inglês de forma divertida, intuitiva e interativa, através de jogos, imagens, perguntas, desafios e exercícios de pronúncia.

A aplicação utiliza conteúdos em **português de Portugal e inglês**, permitindo às crianças aprender novas palavras, compreender o seu significado e descobrir como são pronunciadas.

## 🎮 Principais funcionalidades

### 🧩 1. Descobre a palavra em inglês

Jogo de perguntas e respostas inspirado no jogo «Magia na ponta dos dedos» da VCCAprender.

A criança visualiza uma pergunta e escolhe a resposta correta entre três opções (A/B/C).

**Exemplos:**

**Pergunta:** Como se escreve «Vermelho» em inglês?

- A — Green
- B — Red ✅
- C — Blue

**Pergunta:** Qual é a palavra inglesa para «Gato»?

- A — Dog
- B — Bird
- C — Cat ✅

O jogo inclui imagens ilustrativas, respostas aleatórias, feedback visual, uma tentativa por pergunta e acompanhamento do progresso.

### 🔊 2. Como se diz em inglês?

Uma atividade dedicada à aprendizagem da pronúncia.

A criança seleciona uma palavra ou expressão em português, visualiza a tradução e carrega num botão para ouvir a pronúncia correta em inglês.

**Exemplos:**

| Português | Inglês |
|---|---|
| Bom dia | Good morning |
| Boa noite | Good night |
| Obrigado | Thank you |
| Por favor | Please |
| Olá | Hello |
| Até amanhã | See you tomorrow |
| Gato | Cat |
| Vermelho | Red |

A reprodução de áudio utiliza uma voz inglesa, permitindo ouvir e repetir as palavras quantas vezes forem necessárias, sem sobreposição de reproduções.

### 📚 3. Categorias de aprendizagem

O jogo poderá incluir várias categorias educativas:

- 🎨 **Cores:** aprender os nomes das cores em inglês.
- 🐾 **Animais:** animais domésticos, selvagens, marinhos e respetivos sons.
- 🔢 **Números:** contar, identificar números e realizar pequenas operações.
- 🔤 **Alfabeto:** letras e palavras associadas.
- 🍎 **Frutas e alimentos:** alimentos do quotidiano.
- 🏠 **Casa:** divisões, móveis e objetos.
- 👕 **Vestuário:** roupas e acessórios.
- 🚗 **Transportes:** veículos terrestres, marítimos e aéreos.
- 👨‍👩‍👧 **Família:** membros da família e relações familiares.
- 🧍 **Corpo humano:** partes do corpo.
- 🌦️ **Natureza:** estações do ano, meteorologia, plantas e flores.
- 📅 **Tempo:** dias da semana, meses e horas.
- 🏫 **Escola:** materiais escolares

---

## Desenvolvimento Android — PE-001

A estrutura inicial do plugin é um **APK independente** da aplicação VCCAprender,
com um identificador de pacote próprio. Esta fase disponibiliza um ecrã inicial
de arranque para confirmar que o projeto compila e abre no Android. A integração
com o Game Plugin SDK v1 e os jogos serão adicionados nas issues PE-003 em diante.

| Componente | Versão |
|---|---|
| JDK | 17 |
| Gradle Wrapper | 8.9 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin | 2.0.21 |
| Android SDK (compile/target) | 35 |
| Android minSdk | 26 |
| Package/applicationId | com.vcc.aprender.playenglish |
| Versão inicial | 0.1.0 (versionCode 1) |

**Pré-requisitos:** JDK 17 e Android SDK Platform 35 instalados; definir
`ANDROID_HOME` ou criar um `local.properties` (ignorado pelo Git) apontando
para o SDK, por exemplo `sdk.dir=/caminho/para/android-sdk`.

**Linux / macOS / Codespaces:**

```bash
chmod +x ./gradlew
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

**Windows:**

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```

**APK:** `app/build/outputs/apk/debug/app-debug.apk`.
Pode ser instalado com `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

O Gradle Wrapper (incluindo `gradle-wrapper.jar`) está incluído e utiliza
a distribuição oficial Gradle 8.9, evitando dependência de uma instalação
global de Gradle. A aplicação é independente e pode ser instalada em paralelo
com `com.vcc.aprender`.

**Nota:** este ecrã de arranque é temporário. Nesta issue, o APK ainda
**não é reconhecido como plugin** pela VCCAprender, pois o contrato,
a Activity exportada e as verificações de assinatura pertencem às
issues PE-003 a PE-006. Não acrescentar o módulo ao projeto principal.

---

## CI — PE-009: gerar APK automaticamente no GitHub

O workflow [Android CI - Debug APK](.github/workflows/android-ci.yml) executa automaticamente em **push para `main`** e nos **Pull Requests para `main`**. Também pode ser iniciado manualmente em **Actions → Android CI - Debug APK → Run workflow**, depois de o workflow existir na branch predefinida.

O pipeline:

1. Configura **JDK 17**, Android SDK Platform **35**, Build Tools **35.0.0** e o Gradle Wrapper do projeto (**8.9**).
2. Executa `./gradlew :app:testDebugUnitTest :app:assembleDebug`.
3. Publica os relatórios de testes no artefacto `VCCAprenderPlayEnglish-test-results-<run_number>`.
4. Em caso de sucesso, publica o APK Debug no artefacto **`VCCAprenderPlayEnglish-debug-<run_number>`**.

**Como descarregar:** abrir **Actions**, selecionar a execução concluída com sucesso, ir à secção **Artifacts**, descarregar o ZIP do APK e extrair `app-debug.apk`. Para instalar no Android, usar `adb install -r app-debug.apk` ou o instalador de APK do dispositivo.

Este APK Debug é apenas para desenvolvimento: **não está assinado com a chave Release do host**. A assinatura de produção será tratada em **PE-008/PE-010**. A descoberta e comunicação com a VCCAprender serão implementadas em **PE-003 a PE-006**.

A CI não publica Releases nem incrementa versões automaticamente nesta etapa.

---

## PE-002 — Interface Jetpack Compose, tema infantil e navegação

A Activity de arranque foi convertida para **Jetpack Compose com Material 3**,
mantendo o `applicationId` próprio e o APK independente da VCCAprender.
A interface em português de Portugal utiliza componentes acessíveis, botões
grandes, contraste reforçado, limites de largura para tablets e scroll vertical
para escalas de letra aumentadas.

**Ecrãs e rotas:**

| Rota | Ecrã | Disponibilidade |
|---|---|---|
| `home` | Escolha do modo de aprendizagem | Navegável |
| `quiz` | Descobre a palavra em inglês | Pré-visualização; perguntas na PE-015 |
| `pronunciation` | Como se diz em inglês? | Pré-visualização; voz nas PE-021/023 |
| `results` | Resultados | Pré-visualização; pontuações na PE-020 |

O botão **Voltar** e o botão físico Voltar usam a pilha de navegação.
O `rememberNavController` preserva a navegação durante mudanças de
configuração, incluindo rotação. Os campos do jogo ainda **não** recolhem
respostas nem geram pontuação simulada. O áudio mantém-se desativado até à
implementação do motor Text-to-Speech en-GB.

**Dependências principais:** Jetpack Compose BOM `2024.12.01`, Material 3,
Navigation Compose `2.8.5`, Kotlin Compose Compiler Plugin `2.0.21`
e Activity Compose `1.9.3`.

**Validação:**

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
# Num emulador ou dispositivo com adb:
./gradlew :app:connectedDebugAndroidTest
```

Os testes JVM verificam as rotas; testes instrumentados Compose verificam
**Home → Quiz → Resultados → Home** e **Home → Pronúncia → Voltar**.
O pipeline de CI compila os testes instrumentados, mas a sua execução
requer um emulador ou dispositivo Android.

---

## PE-003 — Integração do Game Plugin SDK v1

O projeto contém agora o módulo **`:game-plugin-sdk`** (Android Library), copiado literalmente do [SDK da VCCAprender](https://github.com/Victor-Candeias/VCCAprender/tree/76127ac5902b361c86271037d3943e737f8e35a7/game-plugin-sdk), no commit fixo `76127ac5902b361c86271037d3943e737f8e35a7`.

- A app incorpora o SDK como `implementation(project(":game-plugin-sdk"))` e continua a compilar **sem checkout adicional**.
- A classe `PlayEnglishPlugin` expõe a identidade estável `com.vcc.aprender.playenglish` e utiliza `GameContract`/`GameValidation`.
- Os testes do SDK oficial foram preservados; acrescentaram-se testes de compatibilidade JVM na app e testes instrumentados de serialização de `Intent`.
- A CI valida o hash Git exato do contrato, compila a biblioteca e executa testes JVM tanto da app como do SDK.

Consultar **[game-plugin-sdk/UPSTREAM.md](game-plugin-sdk/UPSTREAM.md)** para a origem, atualização controlada, regras de versionamento e segurança.

**Compilar e testar:**

```bash
./gradlew :game-plugin-sdk:testDebugUnitTest :game-plugin-sdk:assembleDebug \
    :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
```

**Limites desta fase:** ainda não se declara a `Activity` do plugin, os metadados de descoberta nem a permissão de assinatura no `AndroidManifest.xml` (PE-004/005). Estes testes verificam apenas o contrato e a serialização, não uma ligação real à VCCAprender nem a aceitação do APK pelo host.

---

## PE-004 — Descoberta do plugin pela VCCAprender

O APK contém agora **duas Activities distintas**:

- `MainActivity`: entrada manual para testar os ecrãs Compose diretamente.
- `plugin.PlayEnglishPluginActivity`: entrada reservada à **VCCAprender** e descoberta automaticamente pelo `InstalledGameRegistry` do host.

**Manifesto v1 do plugin:**

| Campo | Valor |
|---|---|
| Action | `com.vcc.aprender.plugin.action.PLAY` |
| Category | `android.intent.category.DEFAULT` |
| GAME_ID | `com.vcc.aprender.playenglish` |
| GAME_TITLE | `VCC Brincar ao Inglês` |
| PROTOCOL_VERSION | `1` (inteiro) |
| Activity | `com.vcc.aprender.playenglish.plugin.PlayEnglishPluginActivity` |
| Exportada | `true` |
| Permissão da Activity / solicitada pelo APK | `com.vcc.aprender.permission.PLAY_GAME` |

A permissão é **definida apenas na VCCAprender**, com `protectionLevel="signature"`: o plugin solicita-a, mas não a redefine. A descoberta pelo host também exige um certificado de assinatura **igual ao do host**, um `GAME_ID` único e metadata válida. Não é utilizado `QUERY_ALL_PACKAGES` nem existe carregamento de código remoto.

A Activity de plugin recusa entradas inválidas e, antes de abrir o ecrã Compose, verifica de forma preventiva o chamador `com.vcc.aprender`, a assinatura, `GameProtocol.readRequest(intent)` e a identidade do jogo. Estes controlos são uma **proteção mínima de arranque**; a gestão completa de sessão e emissão de resultados ficam nas PE-005/006.

**Estado funcional nesta fase:** quando o host inicia uma sessão válida, o plugin apresenta os ecrãs de pré-visualização existentes. Ao fechar, devolve **`RESULT_CANCELED`** e **não guarda pontuações**. Os modos de jogo e voz ainda não foram implementados.

### Como validar

O GitHub Actions compila o APK, executa os testes unitários e confirma no APK resultante os campos essenciais do manifesto. Os testes Android instrumentados (descoberta real por PackageManager) são **compilados na CI**, mas precisam de um dispositivo ou emulador para execução:

```bash
./gradlew :game-plugin-sdk:testDebugUnitTest :app:testDebugUnitTest \
    :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

Para testar no host, instalar ambos os APKs e escolher um perfil na VCCAprender. O jogo só deverá aparecer se os **certificados dos dois APKs forem iguais**. Para comparar os certificados:

```bash
apksigner verify --print-certs VCCAprender-debug.apk
apksigner verify --print-certs VCCAprenderPlayEnglish-debug.apk
```

**Atenção:** o APK Debug gerado pelo GitHub Actions tem a chave Debug do runner, que pode não coincidir com a assinatura do APK host instalado a partir de outra máquina. Para testes reais, compilar/assinar **ambos** no mesmo ambiente ou usar assinaturas Release coincidentes (PE-008/010). A CI do plugin, por si só, não prova descoberta real no host.
