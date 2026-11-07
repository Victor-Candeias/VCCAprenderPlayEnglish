# PE-027 — Números, contagem e formas geométricas

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/28

## Conteúdos
- 21 cardinais de zero a vinte, PT-PT/en-GB, sem mistura de ordinais.
- Dezenas: trinta/thirty, quarenta/forty e cinquenta/fifty.
- 8 figuras: círculo/circle, quadrado/square, triângulo/triangle, retângulo/rectangle, estrela/star, losango/diamond, pentágono/pentagon e hexágono/hexagon.
- A categoria `numbers` inclui 32 entradas e 28 perguntas. Totais gerais: 95 palavras, 71 perguntas.
- 3–5 anos: 0–12 e círculo/quadrado/triângulo; 6–8: inclui todos os conteúdos.
- As 20 perguntas adicionais têm três opções distintas, com a resposta correta apenas uma vez e tradução/audioText en-GB.

## Visual e funcionamento offline
`QuestionIllustration` desenha 1–10 pontos, algarismos para 0 e para 11–20/30/40/50, e glifos individuais para as oito formas. Não exige Internet nem permissões adicionais. Para 0 mostra o algarismo zero. Cada pergunta mantém a regra existente de uma resposta confirmada, feedback e pontuação única.

## Validação
Novo `scripts/test_pe027_numbers_shapes.py`, incluído no CI, valida contagens, textos, idades, unicidade de opções e mapeamento de ilustrações. Testes PE-012/PE-013 atualizados para os totais atuais. Recomenda-se revisão pedagógica e QA visual em dispositivos Android antes de publicação.
