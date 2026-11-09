# PE-030 — Vestuário e meios de transporte

Issue: https://github.com/Victor-Candeias/VCCAprenderPlayEnglish/issues/31

## Entrega

Introduz duas categorias independentes, `clothing` e `transport`, com 12 entradas e 12 perguntas em cada uma, preservando todas as categorias anteriores.

- Vestuário: Camisa/Shirt, Calças/Trousers, Sapatos/Shoes, Casaco/Coat, Vestido/Dress, Chapéu/Hat, Meias/Socks, Saia/Skirt, Camisola/Jumper, Calções/Shorts, Cachecol/Scarf e Luvas/Gloves.
- Transportes: Carro/Car, Comboio/Train, Avião/Plane, Autocarro/Bus, Bicicleta/Bicycle, Barco/Boat, Camião/Lorry, Motociclo/Motorbike, Helicóptero/Helicopter, Elétrico/Tram, Navio/Ship e Trotinete/Scooter.
- As grafias `trousers`, `jumper` e `lorry` seguem o uso britânico.
- Os 24 pictogramas vetoriais são originais, criados para esta funcionalidade e incluídos no APK, sem rede ou imagens de terceiros. As fontes são estes ficheiros XML Android, sem licenças externas.
- TTS usa `audioText` com termo em en-GB; o comportamento real depende da instalação de voz inglesa no dispositivo.
- Cada pergunta usa três escolhas únicas, exclusivas da respetiva categoria, com cobertura de 3 a 8 anos.
- O catálogo passa de 139 para 163 palavras, e de 115 para 139 perguntas.

## Verificação

```sh
python3 scripts/validate_offline_catalog.py
python3 scripts/validate_category_coverage.py
python3 -m unittest discover -s scripts -p "test_pe030_clothing_transport.py" -v
```

CI compila os testes instrumentados Android, mas não executa os testes em dispositivo ou emulador. Confirmar manualmente a legibilidade dos vetores a 200% e a distinção pedagógica das imagens antes de distribuir a crianças.
