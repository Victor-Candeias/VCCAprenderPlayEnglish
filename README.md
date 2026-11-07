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

---

## PE-005 — Validação de segurança no arranque da sessão

A entrada protegida `PlayEnglishPluginActivity` passou a utilizar o validador
`PluginSessionValidator` que aplica a seguinte política **fail-closed**:

1. Confirmar que o **chamador real**, obtido de `Activity.callingPackage`,
   é `com.vcc.aprender` (nunca confiar num extra enviado pelo chamador).
2. Confirmar que `PackageManager.checkSignatures("com.vcc.aprender", packageName)`
   devolve `SIGNATURE_MATCH`; na ausência do host ou em erro, negar.
3. Aceitar apenas um `Intent` **explícito** endereçado à Activity do plugin.
4. Descodificar com `GameProtocol.readRequest` (SDK v1): ação `ACTION_PLAY`,
   versão 1, `GAME_ID=com.vcc.aprender.playenglish`, UUID de sessão canónico
   e `profileId > 0`.
5. Se faltar um campo, tiver tipo incorreto, valor inválido ou os extras
   provocarem uma exceção de leitura, **cancelar e fechar a Activity** sem
   abrir os ecrãs.

O pedido validado contém apenas identificadores opacos; não são enviados nem
registados nomes, idades, avatares ou outros dados da criança. Esta fase
continua a devolver `RESULT_CANCELED`, pois os resultados reais serão
implementados na **PE-006**.

Os testes JVM cobrem chamadores e identificadores inválidos. Os testes
instrumentados Android verificam `Intent` correto e casos de versões,
ações, componentes, tipos e extras inválidos. O CI compila os testes Android,
mas a execução dos testes instrumentados e a confirmação real com o host
dependem de um emulador/dispositivo com os dois APKs assinados pelo
mesmo certificado.

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

---

## PE-006 — Devolução de resultados de um quiz concluído

Foi criado um circuito de comunicação de resultados da Activity do plugin para a
VCCAprender usando **`GameProtocol.createResultIntent(request, correct, total)`**.

1. A Activity aceita apenas um **`GameRequest` validado** pela PE-005.
2. `QuizProgress` recebe uma lista não vazia de IDs únicos das perguntas do jogo
   e contabiliza **uma resposta real por pergunta**; duplicados e IDs estranhos
   são ignorados. Só disponibiliza `QuizCompletion` quando *todas* as perguntas
   foram respondidas, e apenas uma vez.
3. A interface do quiz tem a ligação `onQuizCompleted`, disponível para o
   motor real do quiz (PE-015/PE-020) comunicar a conclusão.
4. `PluginQuizResultSender.createResultOnce` constrói o `Intent` de resultado
   usando os valores de jogo/sessão/perfil do pedido original. O retorno é
   **`RESULT_OK`** seguido de `finish()`, sem duplicação.
5. Se a criança sair antes de concluir o quiz, se o pedido for inválido ou se
   a sessão não tiver respostas completas, o Android mantém
   **`RESULT_CANCELED`** e não envia um resultado fictício.

**Limites do protocolo v1:** `1 <= totalQuestions <= 10000` e
`0 <= correctAnswers <= totalQuestions`. É válido acertar zero perguntas
de um quiz real, mas **não** devolver `0/0`.

**Privacidade:** só são transportados `PROTOCOL_VERSION`, `GAME_ID`,
`SESSION_ID`, `PROFILE_ID`, `CORRECT_ANSWERS` e `TOTAL_QUESTIONS`
em extras primitivos; não se transmitem nomes, idades ou avatares. O host
continua responsável por reverificar o certificado e por persistir cada
sessão associada ao perfil correto, rejeitando resultados repetidos.

**Atenção — integração funcional ainda pendente:** os ecrãs de Quiz continuam
a ser *pré-visualizações*. A callback de conclusão ainda **não é chamada**
enquanto não houver um motor de perguntas reais (PE-015/PE-020). Por isso,
o APK atual **não envia pontuações** apenas por navegar entre ecrãs. A PE-006
implementa e testa o circuito de resultados, não um quiz fictício.

**Validação:**

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

A CI verifica os testes JVM, compila os testes Android e gera o APK. Os testes
instrumentados de `Intent` e o teste E2E de registo pela VCCAprender
requerem um dispositivo/emulador com host e plugin assinados pelo mesmo
certificado. A persistência através de rotação/encerramento do processo
será tratada na PE-007.

---

## PE-007 — Ciclo de vida e cancelamento de sessões

A Activity do plugin utiliza `PluginSessionStateHolder` para manter o estado
transitório de uma sessão do host. A Activity guarda em `onSaveInstanceState`
apenas **gameId**, **sessionId**, **profileId**, estado (ativa/cancelada/
resultado enviado), IDs das perguntas e respostas booleanas. Não guarda nomes,
idades, fotos ou outros dados pessoais.

- **Rotação / recriação do processo:** a Activity volta a validar o chamador e
  o pedido original com a PE-005; `PluginSessionSavedState` recupera o estado
  só quando coincide exatamente com a identidade da sessão e os dados guardados
  são válidos. O Navigation Compose conserva a pilha de ecrãs, e as respostas
  são repostas sem duplicados.
- **Voltar / sair antecipadamente:** o ecrã inicial oferece **Sair do jogo sem
  guardar** quando chamado pelo host. Os botões de Voltar nas vistas do host
  cancelam e fecham o plugin. O botão físico Voltar pode regressar primeiro ao
  ecrã inicial, mas sair da Activity continua a devolver `RESULT_CANCELED`.
- **Conclusão real:** apenas `QuizProgress` com todas as respostas registadas
  pode produzir o `GameProtocol.createResultIntent`. O gestor conserva o estado
  `RESULT_SENT` para impedir nova emissão após recriação; após cancelamento
  não permite criar resultados. Não é emitido `0/0`.
- **Falha de restauração:** se o estado estiver incompleto, corrompido ou
  associado a outra sessão/perfil, a Activity termina com
  `RESULT_CANCELED`, sem reiniciar o quiz como se fosse uma sessão nova.
- **Limites de segurança:** no máximo 500 IDs de perguntas, até 80 caracteres
  por ID no gestor de sessão, para limitar o tamanho dos `Bundle` do Android.
  O protocolo permite até 10 000 perguntas, mas esse limite maior não é
  adequado ao `SavedState` de uma Activity.

**Modo pronúncia:** continua a ser apenas prática; o protocolo v1 exige
contagem de respostas de um quiz real. Não são enviados resultados falsos para
representar escuta ou repetição. Um histórico específico de pronúncia exigiria
a evolução do contrato entre plugin e host.

**Integração ainda pendente:** a UI de Quiz continua em pré-visualização.
As callbacks `onQuizStarted`, `onAnswerRecorded` e `onQuizCompleted`
ficam preparadas para o motor real da PE-015/PE-020; a UI atual não as chama
para fabricar uma pontuação. A confirmação E2E depende de host e plugin com
certificados coincidentes.

**Testes:**

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

A CI executa os testes JVM e compila os testes instrumentados Android;
os testes Android reais exigem emulador/dispositivo.

---

## PE-008 — Assinatura Debug e Release compatível com VCCAprender

A configuração do plugin usa os mesmos nomes de variáveis `VCC_RELEASE_*`
e o mesmo ficheiro ignorado `keystore.properties` da aplicação principal.
`assembleRelease` requer a keystore de Release legítima; sem credenciais a
compilação falha, sem gerar uma Release supostamente compatível.

O Debug pode ser compilado com a keystore padrão Android do **mesmo
utilizador/máquina que compila o host**, ou configurado com `VCC_DEBUG_*` para
uma keystore Debug partilhada. O APK Debug do GitHub Actions não tem
necessariamente o mesmo certificado que a VCCAprender instalada.

O script `scripts/compare-apk-certificates.sh` verifica com
`apksigner` as assinaturas e compara os certificados SHA-256. O workflow
Debug também valida a assinatura do APK gerado, sem recorrer a secrets Release.

**Consultar [docs/PE-008-SIGNING.md](docs/PE-008-SIGNING.md)** para instruções
completas, exemplos, impressão SHA-256 pública do host, políticas de segurança
e passos de validação num dispositivo.

---

## PE-010 — Pipeline Release e versionamento automático

O workflow [Android Release - Signed APK](.github/workflows/android-release.yml)
é acionado **automaticamente a cada push/merge na `main`** (e pode ser
executado manualmente). Utiliza a keystore Release da VCCAprender guardada em
GitHub Secrets para assinar, atribui versões `MAJOR.MINOR.PATCH`, verifica
o certificado SHA-256 do host e publica nos Actions Artifacts o ficheiro
**`VCC-Brincar-ao-Ingles-vX.Y.Z-release.apk`**. A tag `vX.Y.Z` só é
criada depois do upload bem-sucedido. Sem tags anteriores, a primeira
versão é `v0.1.0`. Um erro de assinatura/credenciais bloqueia a Release
sem qualquer fallback para Debug.

As credenciais permanecem em GitHub Secrets; o pipeline não faz fallback
para certificados Debug. Para preparar os secrets e executar uma Release,
consultar **[docs/PE-010-RELEASE.md](docs/PE-010-RELEASE.md)**.

---

## PE-011 — Arquitetura MVVM e lógica offline desacoplada

Foi introduzida uma separação entre **domínio** (estado imutável e casos de uso),
**dados** (catálogo JSON offline), **UI** (Compose e `QuizViewModel`),
**áudio** (contrato) e **plugin** (SDK e segurança do host). A UI não depende
dos modelos de sessão da VCCAprender. O catálogo começará vazio até PE-012;
nenhuma pontuação fictícia é gerada.

Consultar [docs/PE-011-ARCHITECTURE.md](docs/PE-011-ARCHITECTURE.md).

---

## PE-012 — Vocabulário e perguntas disponíveis offline

O catálogo `app/src/main/assets/content/quiz-v1.json` está empacotado no
APK e utiliza `WordEntry` com traduções PT-PT/EN-GB, categorias, idades,
texto para pronúncia e referências opcionais a imagens locais.

As perguntas usam `optionIds` e `correctId`, resolvidos para opções
legíveis por `AssetsQuizContentRepository`. Foram incluídos 18 exemplos
reais de vocabulário e 6 perguntas, com testes de consistência e Unicode.

**[Documentação da PE-012](docs/PE-012-OFFLINE-CATALOG.md)**.
A UI do Quiz continua em pré-visualização; as sessões apenas devolverão
resultados após a implementação do motor interativo PE-015/PE-020.

---

## PE-013 — Dois modos e conteúdos por categorias

A página inicial oferece agora **«Descobre a palavra em inglês»** (explorar
as palavras offline por categoria) e **«Como se diz em inglês?»** (pré-visualização
do futuro quiz). O catálogo PT-PT/EN-GB passou a conter **72 palavras/expressões
e 48 perguntas**, distribuídas pelas seis categorias existentes. Os dados e os
resultados do host continuam isolados da interface.

Detalhes em [docs/PE-013-HOME-CATEGORIES.md](docs/PE-013-HOME-CATEGORIES.md).

---

## PE-014 — Categorias e níveis locais (3–5 e 6–8 anos)

Os ecrãs **Descobre** e **Como se diz em inglês?** partilham uma seleção de
categoria e nível pedagógico, independente da idade real do perfil da
VCCAprender. Os botões mostram contagens de palavras e perguntas filtradas.
Uma categoria sem palavras/perguntas indica o estado vazio e impede iniciar
conteúdo indisponível. O `QuizViewModel` passa a conter só as perguntas
elegíveis para a combinação escolhida, ainda **sem respostas interativas**
(essas chegam na PE-015/PE-020). A seleção funciona offline.

[Documentação e critérios de aceitação PE-014](docs/PE-014-LEVEL-CATEGORIES.md).

---

## PE-015 — Cartões de perguntas A/B/C (pré-seleção)

O ecrã **Como se diz em inglês?** apresenta agora o enunciado real em
português e exatamente três opções A/B/C em inglês, com cartões
adaptativos e deslocamento vertical. Os utilizadores podem **pré-selecionar**
uma opção e **ver outra pergunta**, sem criar resultados falsos; a
confirmação por pressão prolongada e os feedbacks de acerto/erro ficam para
PE-016/017/018. A vista Compose recebe uma projeção segura, que **não
expõe o identificador da resposta certa**. Perguntas de cores têm
amostra de cor; restantes categorias apresentam ícones ilustrativos.

Detalhes: [docs/PE-015-ABC-QUESTION-UI.md](docs/PE-015-ABC-QUESTION-UI.md).

---

## PE-016 — Confirmar com pressão de 800 ms e uma única tentativa

Agora é necessário **manter um dedo 800 ms** sobre A/B/C para confirmar.
Soltar antes, deslizar, fazer scroll ou pressionar com dois dedos cancela
o gesto. Depois da primeira resposta, os outros cartões ficam bloqueados;
o ViewModel recusa novas tentativas e só permite continuar após confirmar.
TalkBack e teclado dispõem de ativação acessível imediata, usando a mesma
validação anti-duplicados. **Ainda não há feedback de acerto/erro nem envio
de resultados ao host** (PE-017, PE-018 e PE-020).

[Especificação e testes PE-016](docs/PE-016-LONG-PRESS-SINGLE-ATTEMPT.md).

---

## PE-017 — Smile de acerto e Pergunta seguinte / Concluir

Depois de uma resposta **certa** confirmada pela pressão de 800 ms
ou ação acessível, a opção escolhida mostra um **smile feliz 😊** e
**«Muito bem! Resposta certa!»**. O smile permanece até avançar;
a próxima pergunta começa sem opção marcada. **«Acertos: N»** é
calculado a partir das respostas reais já confirmadas, evitando
pontuação duplicada.

Na última pergunta, **«Concluir»** calcula o resultado local
`X de Y` sem o transmitir ainda à VCCAprender (integração PE-020).
Respostas erradas ficam bloqueadas e com mensagem neutra, sem smile;
o feedback de erro será desenvolvido na PE-018.

Detalhes e testes: [PE-017](docs/PE-017-CORRECT-ANSWER-FEEDBACK.md).


---

## PE-018 — Resposta errada e correção educativa

Depois de uma resposta incorreta confirmada, mostra-se **«Resposta errada!»** a vermelho e uma explicação com a palavra inglesa correta. As duas opções erradas desaparecem por completo, incluindo da árvore de acessibilidade; apenas a opção correta permanece visível e bloqueada, sem smile. O botão **Pergunta seguinte** permite avançar sem segunda tentativa. O erro continua contabilizado na pontuação real. Nenhuma solução é divulgada antes da confirmação.

[Especificação e testes PE-018](docs/PE-018-WRONG-ANSWER-FEEDBACK.md).


---

## PE-019 — Baralhamento reproduzível de perguntas e opções

Cada partida interativa escolhe uma seed aleatória e baralha o subconjunto de perguntas da categoria/nível, sem repetição. As opções A/B/C também mudam de posição, mantendo IDs e resposta correta do catálogo; as posições certas são distribuídas equilibradamente. Para testes e diagnóstico, a mesma seed reproduz exatamente o baralho. A validação rejeita opções com rótulos indistinguíveis. O score, os feedbacks e a confirmação de 800 ms mantêm-se. [Detalhes PE-019](docs/PE-019-RANDOMIZED-DECK.md).


---

## PE-020 — Resultados reais entregues à VCCAprender

O modo plugin inicia o progresso do host na primeira confirmação verdadeira, regista uma única resposta por pergunta, calcula o score a partir do domínio e apresenta um resumo. **«Terminar e guardar resultados»** envia a conclusão do quiz pelo GameProtocol v1 com `RESULT_OK`, usando os IDs de sessão e perfil originalmente validados. O envio é único e bloqueado se as contagens do ecrã e da sessão divergirem; cancelar mantém `RESULT_CANCELED`. O modo APK independente apenas apresenta resultados locais, sem qualquer envio. A atividade de pronúncia nunca contribui para as respostas corretas. Consultar [PE-020](docs/PE-020-REAL-HOST-RESULTS.md).


---

## PE-021 — Voz inglesa britânica offline (Text-to-Speech)

O ecrã de pronúncia usa agora o `AndroidEnglishPronunciationOutput` com `Locale.UK` (en-GB), ritmo 0,85 e validação de uma voz inglesa britânica **instalada localmente**. Não aceita substituição silenciosa por en-US, voz PT ou uma voz que dependa de rede. Mostra estado e instruções PT-PT para instalar a voz quando não está disponível. O motor é encerrado ao sair do ecrã e o botão de teste diz «Good morning» em inglês. A seleção de vocabulário completo e reprodução entre vários controlos pertencem às PE-022/023. [Documentação PE-021](docs/PE-021-ENGLISH-TTS.md).


---

## PE-022 — Reprodução exclusiva e desbloqueio por callback real

Os controlos «Ouvir em inglês» e «Repetir pronúncia» partilham o mesmo motor de voz e um `SpeechTurnGate` de exclusão. Durante a reprodução ambos ficam bloqueados; um novo pedido não interrompe nem enfileira áudio. Os callbacks `onDone`, `onStop` e `onError` só libertam o pedido se corresponderem ao ID ativo; callbacks atrasados são ignorados. Um erro de reprodução disponibiliza nova tentativa sem obrigar a reiniciar o ecrã. [Detalhes PE-022](docs/PE-022-EXCLUSIVE-PLAYBACK.md).


---

## PE-023 — Modo Como se diz em inglês?

O ecrã de pronúncia mostra as palavras e expressões do catálogo offline filtradas por categoria/nível, com texto PT-PT, tradução EN-GB e ilustração da categoria. Ouvir e Repetir utilizam exclusivamente o campo inglês audioText e a proteção TTS da PE-022. A navegação Anterior/Seguinte não repete ciclicamente palavras e fica bloqueada durante a fala. Não existe pontuação nem resultado do host. Consultar docs/PE-023-PRONUNCIATION-MODE.md.


## PE-024 — Biblioteca offline com pesquisa e escolha aleatória

No modo de pronúncia, o campo de pesquisa filtra as palavras já escolhidas por nível e categoria, em português ou inglês, ignorando maiúsculas e acentos (`maçã` / `maca`). As listas originais não são modificadas. O botão «Palavra aleatória» escolhe outra entrada quando há pelo menos duas. «Anterior», «Seguinte», «Ouvir» e «Repetir» continuam ligados aos resultados visíveis, usando apenas áudio en-GB. Resultados vazios apresentam uma mensagem, sem pontuação artificial. Ver `docs/PE-024-VOCABULARY-SEARCH.md`.


---

## PE-025 — Cores e tonalidades por faixa etária

O catálogo inclui 15 cores em português de Portugal e inglês britânico, com amostras de cor locais no Quiz: vermelho/red, azul/blue, verde/green e mais doze cores/tonalidades. As novas entradas **prateado/silver**, **bege/beige** e **turquesa/turquoise** são exclusivamente para o nível 6–8; as faixas 3–5 mantêm o vocabulário anterior. Cada nova questão contém três opções distintas e pronúncia `audioText` em inglês britânico. A validação editorial de cobertura e os testes de esquema foram atualizados para 75 palavras e 51 perguntas. Ver `docs/PE-025-COLOURS.md`.


## PE-027 — Números, contagem e formas geométricas

O catálogo offline tem 32 itens em Números: cardinais de 0 a 20, dezenas 30/40/50 e 8 formas (círculo, quadrado, triângulo, retângulo, estrela, losango, pentágono e hexágono). As novas perguntas mostram pontos, algarismos ou símbolos geométricos, com três respostas distintas e voz en-GB. A lista original permanece intacta. Ver docs/PE-027-NUMBERS-COUNTING-SHAPES.md.


## PE-026 — Animais domésticos, selvagens e marinhos

O catálogo contém agora 32 animais bilingues PT-PT/en-GB, com 28 perguntas na categoria. Os 20 novos incluem golfinho/dolphin, tubarão/shark, baleia/whale, foca/seal, polvo/octopus, caranguejo/crab, cavalo-marinho/seahorse, animais domésticos e selvagens. Cada entrada inclui `audioText` inglês para a pronúncia en-GB, ID estável e faixa etária; as perguntas têm três opções distintas e compatíveis. As vistas de quiz e pronúncia usam símbolos específicos para cada animal como representação provisória offline. **Dependência PE-036 em aberto:** produzir imagens vetoriais próprias e empacotadas, com licenças documentadas; símbolos do sistema não substituem esses assets. Ver `docs/PE-026-ANIMALS.md`.
