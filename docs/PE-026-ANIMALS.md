# PE-026 — Animais domésticos, selvagens e marinhos

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/27

## Entrega de conteúdo
- 32 entradas totais na categoria `animals`, 28 perguntas de escolha múltipla (20 novas entradas e perguntas).
- Animais domésticos: gato/cat, cão/dog, cabra/goat, galinha/chicken, galo/rooster, etc.
- Animais selvagens: leão/lion, elefante/elephant, tigre/tiger, zebra/zebra, girafa/giraffe, panda-gigante/giant panda, etc.
- Animais marinhos e costeiros: golfinho/dolphin, tubarão/shark, polvo/octopus, baleia/whale, foca/seal, cavalo-marinho/seahorse, caranguejo/crab.
- Preservados todos os IDs originais. Sem termo genérico ambíguo `tartaruga`; usamos `Panda-gigante` em vez de espécie indefinida.
- Traduções PT-PT e en-GB e `audioText` inglês; TTS e bloqueio de áudio já são providenciados pelas PE-021/022.
- Idades 3–8 para conceitos simples; 6–8 para espécies e nomes mais específicos.

## Perguntas
Cada pergunta nova referencia três entradas da categoria, sem opções repetidas e com sobreposição de idades. O quiz mostra uma ilustração distinta por ID e não revela o nome da resposta antes da confirmação. O catálogo completo passa de 95/71 para **115 palavras e 91 perguntas**.

## Representações visuais: dependência PE-036
`AnimalIllustrations` mapeia 32 espécies para símbolos Unicode/emoji diferentes, utilizados em quiz e pronúncia. Estes glifos são desenhados pelas fontes do sistema, não são ficheiros de imagem empacotados. **Isto é uma solução visual intermédia, não o fecho do critério PE-036.** A PE-036 permanece aberta e exige imagens originais ou licenciadas guardadas no APK, associação a cada `imageRef`, proveniência/licenças e testes de legibilidade e resolução dos assets. Alguns glifos podem variar entre dispositivos; por exemplo, cavalo-marinho usa uma representação genérica de peixe e deverá ser substituído por uma ilustração específica.

## Testes
`scripts/test_pe026_animals.py` valida cobertura 30+, traduções PT/en, consistência TTS, opções distintas, idades e mapeamento visual. Os testes de catálogo e cobertura foram atualizados para os totais 115/91. Teste de fala real e QA visual em emulador/dispositivo continuam necessários.

## Critérios ainda dependentes
A validação de ilustrações licenciadas/originais específicas de cada animal é bloqueada pela PE-036; não declarar a PE-026 pedagogicamente concluída sem esta etapa.
