# HAR Converter

Aplicação Spring Boot (Java 8) que oferece serviço REST para converter arquivos `.har` em `.csv` e também permite a conversão via linha de comando.

## Requisitos

- Java 8
- Maven 3.8+

## Executar testes

```bash
mvn test
```

## Executar aplicação Web

```bash
mvn spring-boot:run
```

Após iniciar, acesse a documentação interativa Swagger em [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).

### Conversão via REST

Envie uma requisição `POST` para `http://localhost:8080/api/har/convert` com um arquivo `.har` no campo `file` (multipart/form-data). O serviço retornará o arquivo CSV pronto para download.

## Conversão via linha de comando

Também é possível executar a conversão diretamente pela linha de comando:

```bash
mvn -q -DskipTests package
java -jar target/har-converter-0.0.1-SNAPSHOT.jar --har-file=/caminho/para/arquivo.har --csv-file=/caminho/para/saida.csv
```

- `--har-file` (obrigatório): caminho para o arquivo `.har` de entrada.
- `--csv-file` (opcional): caminho para o arquivo `.csv` de saída. Se omitido, o arquivo será criado com o mesmo nome do `.har` na mesma pasta.

O processo exibirá o caminho absoluto do CSV gerado e encerrará a aplicação após a conversão.
