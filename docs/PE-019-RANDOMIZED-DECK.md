# PE-019 — Baralho de perguntas e respostas com seed

**Issue:** https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/20

## Implementação
- `ShuffleQuizDeckUseCase.execute(questions, seed)` usa `kotlin.random.Random(seed)` e executa baralhamento Fisher–Yates pelo `shuffled(random)`.
- Cada ID de pergunta é único no baralho; cada pergunta mantém os três IDs originais, o `correctChoiceId` e as etiquetas originais.
- As posições certas são distribuídas em A/B/C por ciclos de três, com ordem do ciclo também baralhada pela seed. O desequilíbrio máximo por posição é 1.
- As duas opções incorretas são permutadas e inseridas em redor da correta; todos os rótulos devem ser distintos ignorando maiúsculas/espaços periféricos.
- A UI real envia uma seed fresca em cada mudança de seleção, enquanto `selectQuestionSet(ids, seed)` permite reproduzir um caso de erro. O overload sem seed preserva a ordem legada para consumidores programáticos e testes antigos.
- O domínio valida respostas pelos **IDs**, nunca pela letra da posição. Mantêm-se a tentativa única, os feedbacks PE-017/018 e o `QuizScore` real.

## Testes
`ShuffleQuizDeckUseCaseTest`: seed idêntica produz a mesma sequência, seeds diferentes produzem sequências distintas, IDs sem repetição, posições A/B/C balanceadas, integridade da resposta certa, erro de rótulos duplicados, confirmação única e score real. Os testes instrumentados deixam de assumir uma posição fixa da resposta correta.

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
# Dispositivo Android necessário:
./gradlew :app:connectedDebugAndroidTest
```

O CI compila os testes instrumentados mas não os executa em dispositivo. O envio de resultados ao host continua fora do âmbito (PE-020).
