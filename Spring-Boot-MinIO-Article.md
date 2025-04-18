# Integração do Spring Boot com MinIO: Uma Solução Completa para Armazenamento de Objetos

## Introdução

No mundo atual de desenvolvimento de aplicações, o armazenamento eficiente de arquivos e objetos é uma necessidade crítica. Muitas vezes, precisamos de uma solução que seja escalável, segura e compatível com padrões de mercado. É nesse contexto que o MinIO se destaca como uma excelente alternativa para armazenamento de objetos.

Este artigo explora um projeto Spring Boot que integra com o MinIO, oferecendo uma solução completa para gerenciamento de arquivos. O projeto implementa operações essenciais como upload, download, listagem e exclusão de arquivos, além de gerenciamento de buckets, tudo através de uma API REST bem estruturada.

## O que é MinIO?

MinIO é um servidor de armazenamento de objetos de alto desempenho, compatível com a API Amazon S3. Algumas características importantes do MinIO incluem:

- É o único conjunto de armazenamento de objetos disponível em todas as nuvens públicas com suporte de alto desempenho
- Compatibilidade total com a API do Amazon S3, facilitando a migração entre ambientes
- Capacidade de lidar com dados não estruturados como fotos, vídeos, arquivos de log, backups e imagens de contêiner
- Suporte para objetos de até 5TB
- Código aberto e fácil de implantar em qualquer ambiente

## Arquitetura do Projeto

O projeto segue uma arquitetura em camadas típica de aplicações Spring Boot:

1. **Camada de Controlador (Controller)**: Responsável por expor os endpoints REST e lidar com as requisições HTTP
2. **Camada de Serviço (Service)**: Contém a lógica de negócio e orquestra as operações
3. **Camada de Utilitários (Util)**: Fornece funcionalidades de baixo nível para interagir diretamente com o cliente MinIO
4. **Camada de Configuração (Config)**: Configura os beans e propriedades necessários para a aplicação

### Diagrama de Componentes

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │      │                 │
│  Controller     │──────│  Service        │──────│  Util           │──────│  MinIO Client   │
│  (REST API)     │      │  (Business      │      │  (Low-level     │      │  (External      │
│                 │      │   Logic)        │      │   Operations)   │      │   Service)      │
└─────────────────┘      └─────────────────┘      └─────────────────┘      └─────────────────┘
```

## Principais Funcionalidades

O projeto implementa as seguintes funcionalidades:

1. **Gerenciamento de Buckets**
   - Criação de buckets
   - Listagem de todos os buckets
   - Exclusão de buckets

2. **Gerenciamento de Objetos**
   - Upload de arquivos
   - Download de arquivos
   - Listagem de objetos em um bucket
   - Exclusão de objetos individuais
   - Exclusão de múltiplos objetos
   - Obtenção de URLs para acesso aos objetos

## Implementação Técnica

### Configuração do MinIO

A configuração do MinIO é feita através da classe `MinioConfig`, que utiliza propriedades definidas no arquivo `application.yml`:

```java
@Data
@Configuration
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {
    private String endpoint;
    private Integer port;
    private String accessKey;
    private String secretKey;
    private boolean secure;
    private String bucketName;
    private long imageSize;
    private long fileSize;

    @Bean
    public MinioClient minioClient() {
        MinioClient minioClient =
                MinioClient.builder()
                        .credentials(accessKey, secretKey)
                        .endpoint(endpoint, port, secure)
                        .build();
        return minioClient;
    }
}
```

### API REST

A API REST é implementada na classe `MinioController`, que expõe endpoints para todas as operações suportadas:

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/minio")
@Tag(name = "Minio Controller", description = "API para gerenciamento de arquivos no MinIO")
public class MinioController {
    // Implementação dos endpoints
}
```

### Operações com MinIO

As operações com o MinIO são implementadas na classe `MinioServiceImpl`, que utiliza a classe `MinioUtil` para interagir com o cliente MinIO:

```java
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioUtil minioUtil;
    private final MinioConfig minioProperties;
    
    // Implementação das operações
}
```

### Tratamento de Exceções

O projeto implementa um tratamento de exceções global através da classe `GlobalExceptionHandler`, que captura exceções e retorna respostas HTTP apropriadas:

```java
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    // Implementação dos handlers de exceção
}
```

### Programação Orientada a Aspectos (AOP)

O projeto utiliza AOP para adicionar comportamentos transversais aos métodos do serviço MinIO:

```java
@Aspect
@Component
public class MinioServiceImplAspect {
    @Before(value="execution(* com.springminio.app.service.MinioService.*(..))")
    public void beforeAdvice(JoinPoint joinPoint){
        System.out.println("MinioServiceImplAspect | Before MinioService method got called");
    }
    
    // Outros advices
}
```

## Como Executar o Projeto

### Pré-requisitos

- Docker Desktop instalado
- Java 17 ou superior
- Maven

### Passos para Execução

1. Clone o repositório
2. Execute o MinIO no Docker:
   ```
   docker-compose up -d
   ```
3. Acesse o MinIO no navegador:
   ```
   http://127.0.0.1:9001
   ```
4. Faça login no MinIO:
   ```
   username: minioadmin
   password: minioadmin
   ```
5. Execute a aplicação Spring Boot:
   ```
   mvn spring-boot:run
   ```
6. Acesse a documentação da API:
   ```
   http://localhost:8085/swagger-ui.html
   ```

## Exemplos de Uso da API

### Upload de Arquivo

```
POST /minio/upload
```
Parâmetros:
- `file`: Arquivo a ser enviado
- `bucketName`: Nome do bucket

### Download de Arquivo

```
GET /minio/download/{bucketName}/{objectName}
```

### Listar Objetos em um Bucket

```
GET /minio/show/{bucketName}
```

### Obter URL de um Objeto

```
GET /minio/showURL/{bucketName}/{objectName}
```

## Destaques Técnicos

### Padrões de Projeto

O projeto utiliza vários padrões de projeto:

1. **Injeção de Dependência**: Utilizado em toda a aplicação através das anotações `@RequiredArgsConstructor` do Lombok
2. **Facade**: A interface `MinioService` atua como uma fachada para as operações complexas do MinIO
3. **Strategy**: Diferentes implementações podem ser fornecidas para a interface `MinioService`
4. **Decorator**: AOP é utilizado para "decorar" os métodos do serviço com comportamentos adicionais

### Boas Práticas

O projeto segue várias boas práticas de desenvolvimento:

1. **Separação de Responsabilidades**: Cada classe tem uma responsabilidade bem definida
2. **Tratamento de Exceções**: Exceções são tratadas de forma centralizada
3. **Logging**: Logging extensivo para facilitar a depuração
4. **Documentação da API**: Utilização do Swagger para documentar a API
5. **Configuração Externalizada**: Configurações são definidas no arquivo `application.yml`

## Conclusão

A integração do Spring Boot com o MinIO oferece uma solução robusta e flexível para armazenamento de objetos. O projeto apresentado neste artigo demonstra como implementar essa integração de forma estruturada e seguindo boas práticas de desenvolvimento.

Com esta solução, é possível gerenciar arquivos de forma eficiente, aproveitando a escalabilidade e compatibilidade do MinIO com o padrão S3, tudo isso com a produtividade e facilidade de uso do Spring Boot.

Este projeto pode ser utilizado como base para implementações mais complexas, como sistemas de gerenciamento de conteúdo, armazenamento de backups, ou qualquer aplicação que necessite de armazenamento de objetos confiável e escalável.

## Referências

- [Documentação oficial do Spring Boot](https://spring.io/projects/spring-boot)
- [Documentação oficial do MinIO](https://min.io/docs/minio/linux/index.html)
- [API do MinIO para Java](https://docs.min.io/docs/java-client-api-reference.html)
- [Padrões de Projeto](https://refactoring.guru/design-patterns)