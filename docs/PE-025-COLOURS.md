# PE-025 — Banco bilingue de cores e tonalidades

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/26

## Conteúdo offline
O catálogo v1 preserva IDs e entradas existentes. Adiciona:
- `colors.silver`: Prateado / Silver (6–8 anos), swatch #B0BEC5.
- `colors.beige`: Bege / Beige (6–8 anos), swatch #D9C5A0.
- `colors.turquoise`: Turquesa / Turquoise (6–8 anos), swatch #008C95.

O total é de 15 cores, 75 palavras no catálogo e 51 perguntas. As três perguntas usam opções da mesma categoria, com IDs únicos e traduções inequivocamente diferentes. A faixa 3–5 mantém as 12 cores de base; a faixa 6–8 recebe as três tonalidades adicionais.

## UI e voz
`QuizScreen.QuestionIllustration` gera amostras de cor locais com valores ARGB estáveis; nenhuma imagem remota nem acesso à Internet. O campo `audioText` está em inglês britânico e é utilizado pelo modo de pronúncia PE-023. As novas cores não fazem parte de categorias diferentes.

## Testes
`scripts/test_pe025_colours.py` verifica traduções, 15 amostras, faixa etária, perguntas com 3 opções e coerência de prompts. Atualizados testes de cobertura e de totais do catálogo para evitar valores fixos antigos.

O CI executa os validadores Python, testes JVM e compilação Android, mas não executa testes instrumentados num emulador. Recomenda-se revisão pedagógica das tonalidades e QA visual com crianças antes de distribuição.
