# PE-031 — Família e partes do corpo humano

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/32

## Conteúdo e linguagem

São introduzidas 2 categorias com 12 termos cada, num total de 24 traduções PT-PT/en-GB e 24 perguntas com 3 respostas distintas.

- Família: Mãe/Mother, Pai/Father, Irmã/Sister, Irmão/Brother, Avó/Grandmother, Avô/Grandfather, Tia/Aunt, Tio/Uncle, Primo ou prima/Cousin, Bebé/Baby, Pais/Parents, Família/Family.
- Corpo humano: Cabeça/Head, Mão/Hand, Pé/Foot, Olho/Eye, Orelha/Ear, Nariz/Nose, Boca/Mouth, Braço/Arm, Perna/Leg, Dedo da mão/Finger, Joelho/Knee, Ombro/Shoulder.
- `family.cousin` usa «Primo ou prima» para evitar assumir o género associado a «cousin».
- A família pode ter diferentes composições. O quiz usa a etiqueta textual como significado e os pictogramas não pretendem deduzir relações familiares pela aparência de uma pessoa.
- Representações de pessoas são esquemáticas, sem símbolos sexuais, roupas específicas de género, cor de pele identificável ou julgamento moral.
- Todas as imagens são vetores originais incluídos no APK, sem rede nem conteúdos de terceiros. Recomenda-se revisão pedagógica e visual antes de utilização infantil.
- Síntese de voz usa o campo `audioText` (en-GB), condicionada ao motor e voz local disponíveis.
- Estão preservadas todas as entradas e IDs já existentes. Totais: 187 palavras e 163 perguntas.

## Validação

```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p "test_pe031_family_body.py" -v
```

O workflow Android CI executa os testes Python e unitários Android, e compila `androidTest`, mas não executa os testes instrumentados num dispositivo/emulador.
