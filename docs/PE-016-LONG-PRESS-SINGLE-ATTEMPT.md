# PE-016 — Confirmação por pressão prolongada e tentativa única

**Issue:** [#17](https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/17)  
**Prioridade:** P0 · **MVP** · **Dependência:** PE-015

## Regras do gesto (Finger Magic)

- **Toque físico de um dedo:** manter a opção A/B/C pressionada durante
  **800 milissegundos contínuos**. Uma vez ultrapassado o limiar, a opção é
  confirmada exatamente uma vez pelo `QuizViewModel.confirmOption()`.
- **Soltar antes de 800 ms:** cancela, não responde e não revela o acerto/erro.
- **Mover o dedo além de 2 × touchSlop, iniciar scroll, ou um segundo dedo
  pressionado:** cancela a tentativa, mesmo que a pressão continue.
- **Double tap / eventos repetidos:** após a primeira confirmação, o domínio
  já guarda a opção e rejeita todas as posteriores, incluindo outra opção.
- **Após a confirmação:** os três cartões ficam bloqueados; apenas o
  identificador da opção escolhida é realçado e é apresentada a mensagem
  neutra «Resposta registada». Nenhuma opção correta é revelada.
- Só é possível continuar para a **próxima pergunta não respondida** após
  confirmar a atual. Não se volta à primeira pergunta e não se consegue
  ignorar uma pergunta sem tentativa.
- Ao mudar o nível/categoria, a PE-014 reinicia a lista e limpa respostas.

## Acessibilidade e dispositivos

O gesto físico é implementado em `Modifier.pointerInput`, usando
`awaitEachGesture`, `awaitFirstDown` e
`AwaitPointerEventScope.withTimeoutOrNull(800L)`. A API de timeout
corresponde ao relógio do gesto, não ao long-press do sistema Android.

TalkBack oferece ações semânticas **Confirmar esta resposta** (`onClick`
e `onLongClick`), sem obrigar a uma pressão física cronometrada.
Teclado, D-pad e dispositivos assistivos podem confirmar com
**Enter, Espaço ou o botão central do D-pad** depois de focar o cartão.
Todos estes canais chamam **o mesmo `confirmOption`**, que rejeita
duplicados. Os cartões têm descrição de estado e são desativados após
a confirmação. A alternativa de acessibilidade é deliberadamente
imediata (a exigência dos 800 ms aplica-se exclusivamente ao toque físico).

O layout mantém os cartões responsivos da PE-015 (mín. 80 dp de altura,
fonte escalável e scroll vertical).

## Separação das próximas etapas

O `AnswerQuestionUseCase` guarda uma única escolha real no
`QuizState.selected` (ID da questão → ID da opção). A resposta correta
continua no modelo de domínio e não é disponibilizada à projeção
`QuizQuestionPreview` usada pela UI.

- **PE-017:** mostrar smile e feedback positivo quando acertar.
- **PE-018:** feedback vermelho e revelar apenas a opção correta quando errar.
- **PE-019:** aleatorização de perguntas e distratores.
- **PE-020:** conclusão, score final e emissão controlada de resultado ao host.

O registo de uma resposta **não** cria já um resultado do host nem fabrica
pontuações. O botão de resultados mantém-se pré-visualização e só fica
acessível depois de todas as perguntas terem resposta.

## Validação

### Unitários (CI)
`QuizViewModelTest` valida:
- uma única tentativa, mesmo após 30 eventos repetidos;
- erros e acertos guardados por ID, sem score antecipado;
- recusa de pergunta invisível/ID desconhecido;
- navegação bloqueada até confirmar, sem regressar ou saltar perguntas;
- estado cancelado/concluído rejeita novas confirmações;
- mudança de filtro reinicia as confirmações.

### Android instrumentados
`PlayEnglishNavigationTest` valida toque curto físico sem resposta,
ativação semântica por acessibilidade, bloqueio dos restantes cartões
e avanço somente depois de responder. Estes testes são **compilados no
CI mas não executados** em emulador/dispositivo.

### QA de gestos (a executar num Android real/emulador)
1. Manter 700 ms e soltar: 0 respostas.
2. Manter 850–1000 ms continuamente: 1 resposta.
3. Tentar dois dedos e deslizar: 0 respostas.
4. Dois pressionamentos simultâneos, double click e Enter repetido: 1
   resposta máxima.
5. TalkBack: ativar opção anunciada e confirmar sem cronometragem.
6. Ecrã a 200% de fonte: todos os cartões acessíveis por scroll.

```bash
./gradlew :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

O sucesso do CI não substitui os testes reais de toque/temporização.
