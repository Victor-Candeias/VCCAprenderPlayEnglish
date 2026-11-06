# PE-017 — Feedback positivo: smile, Pergunta seguinte e Concluir

**Issue:** [#18](https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/18) · **MVP P0**  
**Dependência:** PE-016 (800 ms e tentativa única)

## Regras implementadas

- Antes da confirmação de resposta, a projeção UI apresenta
  `wasCorrect = null`: não revela o acerto nem o ID correto.
- Depois de confirmar uma opção correta, `wasCorrect = true` e
  **aparece um smile feliz 😊 por cima do texto da opção escolhida**,
  com confirmação curta **«Muito bem! Resposta certa!»** em verde.
- O smile fica **visível sem temporizador** até selecionar
  **«Pergunta seguinte»**; recomposições do Compose e toques
  adicionais não o apagam nem adicionam pontos.
- Se a resposta for incorreta, `wasCorrect = false`: não aparece
  smile ou texto positivo; mantêm-se os três cartões bloqueados e a
  mensagem neutra «Resposta registada». A PE-018 trata das mensagens
  e do conteúdo explicativo específicos para erros.
- O indicador **«Acertos: N»** conta apenas perguntas cujo
  `QuizState.selected[id]` corresponde a `correctChoiceId` no
  modelo de domínio, evitando incremento por duplos cliques e
  eventos repetidos de TalkBack/teclado.
- **«Pergunta seguinte»** só é ativado após confirmar a resposta
  corrente. Ao avançar, `wasCorrect = null` e a próxima pergunta
  começa sem opção marcada; o total acumulado mantém-se.
- Depois da última pergunta confirmada, surge **«Concluir»**;
  `CompleteQuizUseCase` calcula o `QuizScore` real e mostra
  «Acertaste X de Y perguntas» no ecrã local de resultados.

## Limites e segurança

`QuizQuestionPreview` continua sem `correctChoiceId`.
O ecrã só conhece o booleano `wasCorrect` **depois** de existir
uma escolha efetivamente confirmada na camada de domínio.
O contrato do host VCCAprender permanece inalterado:
**não há callback `onQuizCompleted`, `RESULT_OK`, envio ou
persistência de score nesta fase**. A integração segura com
a sessão da VCCAprender permanece na **PE-020**.

Também permanecem fora do âmbito desta entrega o feedback de erro
(PE-018) e o baralhamento de opções (PE-019).

## Testes

### JVM
`QuizViewModelTest` cobre ausência de sucesso antes da confirmação,
contagem única após mais de 30 eventos repetidos, erro sem smile,
persistência do sucesso até avançar, reset visual na nova pergunta,
conclusão bloqueada enquanto existirem perguntas por responder,
score final real e reset por categoria/nível.

### Android instrumentado
`PlayEnglishNavigationTest` cobre a opção B correta da primeira
pergunta `animals.cat`, smile visível e persistente até avançar,
botões bloqueados, questão seguinte sem smile, resposta errada
`colors.red` (A) sem mensagem positiva. Corrige ainda os
identificadores de teste das mensagens da PE-016.

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
# Dispositivo ou emulador Android necessário:
./gradlew :app:connectedDebugAndroidTest
```

O CI executa testes JVM e compila os testes instrumentados, mas
**não executa** os testes instrumentados num Android. O smile em
tamanhos de fonte de 200%, os 800 ms físicos, TalkBack e a
navegação devem ser validados em dispositivo/emulador.
