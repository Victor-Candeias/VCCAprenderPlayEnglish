# PE-034 — Emoções, ações e verbos simples

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/35

## Conteúdo implementado

Duas categorias (`emotions`, `actions`) com **15 termos/perguntas cada** (30 novos itens e 30 quizzes). 12 conceitos básicos em cada categoria, mais 3 frases simples por categoria.

**Emoções:** Feliz/Happy; Triste/Sad; Zangado ou zangada/Angry; Cansado ou cansada/Tired; Entusiasmado ou entusiasmada/Excited; Calmo ou calma/Calm; Surpreendido ou surpreendida/Surprised; Preocupado ou preocupada/Worried; Orgulhoso ou orgulhosa/Proud; Tímido ou tímida/Shy; Curioso ou curiosa/Curious; Descontraído ou descontraída/Relaxed.

**Ações:** Correr/Run, Andar/Walk, Saltar/Jump, Ler/Read, Cantar/Sing, Brincar/Play, Dançar/Dance, Escrever/Write, Desenhar/Draw, Nadar/Swim, Comer/Eat, Dormir/Sleep.

**Frases de modelo:** Estou feliz/I am happy; Estou triste/I am sad; Estou cansado ou cansada/I am tired; Eu consigo correr/I can run; Eu consigo saltar/I can jump; Eu consigo cantar/I can sing.

Todas as palavras têm tradução PT-PT/en-GB, pronúncia TTS por `audioText` e imagem vetorial original em recursos `drawable/pe_emotions_*` ou `drawable/pe_actions_*`, empacotada sem necessidade de rede. 30 ficheiros de ilustrações, com variação de expressões faciais, poses e objetos para diferenciar cada conceito.

## Adequação pedagógica

- Emoções são estados normais, não rótulos morais da criança; as formas PT-PT de género são inclusivas quando necessário.
- Emoções e ações isoladas: idades 3 a 8.
- Frases mais longas: apenas idades 6 a 8.
- Cada pergunta tem três opções distintas do respetivo grupo, nunca confundindo frases com palavras isoladas.
- A expressão facial é um exemplo ilustrativo; não é uma forma segura de inferir emoções reais de uma pessoa. A revisão pedagógica deve confirmar legibilidade, acessibilidade e variação da representação.
- Alguns verbos apresentam infinitivo isolado (e.g. Run) e as frases acrescentam contexto; a revisão linguística deve validar o enquadramento didático.

## Validação

```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p "test_pe034_emotions_actions.py" -v
```

Totais globais estimados após merge: **276 palavras, 252 perguntas e 16 categorias**. A pipeline CI compila testes Android instrumentados sem os executar num dispositivo. Executar revisão visual/pedagógica e verificação TTS num Android antes da distribuição.
