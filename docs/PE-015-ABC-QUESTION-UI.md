# PE-015 — Interface A/B/C para perguntas offline

**Issue:** [#16](https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/16)  
**Fase:** Jogo · **Prioridade:** P0 · **Dependências:** PE-012/013/014

## O que foi implementado

1. Depois de escolher nível e categoria (PE-014), mostra-se uma **pergunta
   PT-PT verdadeira** do `quiz-v1.json`, com as três traduções de resposta
   em **EN-GB** na ordem original do catálogo. Cada opção tem o marcador
   **A, B ou C**, texto legível e um cartão clicável completo.
2. **Pré-seleção por toque**: tocar numa opção altera o destaque visível
   (cor menta + contorno) e descrição acessível `Pré-selecionada`.
   Tocar noutra opção move o destaque, sem atribuir um acerto/erro nem gravar
   uma resposta. Esta pré-seleção será substituída na PE-016 pela confirmação
   por pressão prolongada (~800 ms).
3. A opção `Ver outra pergunta (pré-visualização)` percorre ciclicamente
   as perguntas da categoria/nível **sem pontuação e sem confirmação**.
   Ao mudar de pergunta ou filtro a pré-seleção desaparece.
4. As perguntas sobre cores mostram uma **amostra circular da cor**
   correspondente ao enunciado PT-PT, sem rotular a resposta em inglês.
   As restantes mostram ilustrações emoji da categoria, sem dependências
   remotas, imagens por descarregar ou assets fictícios. O catálogo original
   mantém `imageRef=null`; quando existirem imagens próprias empacotadas,
   o UI pode preferi-las (tarefa posterior).
5. Os cartões usam `OutlinedButton` com contraste elevado e fundo
   branco/menta, **sem quadrados pretos separados**, mínimo 80 dp de
   altura e conteúdo flexível. `ChildScreenLayout` mantém **scroll vertical**,
   permitindo fontes a 200% sem fixar altura do texto.

## Segurança e integração

- `QuizUiState.preview` converte o modelo de domínio numa
  **`QuizQuestionPreview` que não contém `correctChoiceId` nem qualquer
  indicação da resposta certa**; o ecrã Compose só recebe esta projeção.
  Os cartões nunca divulgam o estado correto nos textos, teste de semântica
  ou TalkBack antes de a resposta ser confirmada.
- O campo `previewSelectedOptionId` representa apenas destaque visual.
  `QuizViewModel.previewOption()` **não chama** `AnswerQuestionUseCase`,
  não altera `QuizState.selected` e não envia callbacks do host.
- A pergunta apresentada tem de estar na lista filtrada da PE-014;
  durante a mudança de categoria/nível o UI impede temporariamente que
  apareça a pergunta antiga.
- Não há acesso à rede nem alteração às permissões, certificado ou contrato
  VCCAprender Plugin SDK v1. Nenhuma pontuação ou `RESULT_OK` é fabricada.

## Limites propositados desta PE

As próximas issues introduzem as regras definitivas:
- **PE-016:** confirmação por pressão prolongada e uma única tentativa.
- **PE-017:** feedback de acerto e avanço depois de confirmação.
- **PE-018:** feedback de erro e exclusão das opções incorretas.
- **PE-019:** baralhar perguntas e respostas com seed reproduzível.
- **PE-020:** conclusão/score e comunicação de resultados reais ao host.

A navegação para o ecrã de resultados ainda é uma **pré-visualização do
layout** (texto placeholder); nunca equivale a concluir uma sessão real.

## Testes e comandos

Os testes JVM cobrem: três opções na ordem correta, ausência de
`correctChoiceId` na projeção UI, toque que não pontua, recusa de IDs
desconhecidos, mudança de pergunta com reset da pré-seleção e alteração
de categoria que reinicia o estado.

Os testes Android instrumentados verificam os três cartões A/B/C, o
enunciado PT-PT, a amostra de cor, a transição de pergunta, o estado vazio
e a ausência de feedback antes da confirmação.

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

O pipeline CI executa JVM, compila os testes instrumentados e publica o
APK Debug. O teste de scroll com fonte 200% deve ser confirmado num
emulador/dispositivo com **Definições → Tamanho do texto** ajustado;
o CI não executa testes instrumentados Android.
