# Spring Boot Minio

<img src="screenshots/springboot_minio.png" alt="Main Information" width="800" height="300">

### 📖 Informação

<ul style="list-style-type:disc">
<li>MinIO é o único conjunto de armazenamento de objetos disponível em
       todas as nuvens públicas com suporte de alto desempenho.</li>
   <li>É API compatível com o serviço de armazenamento em nuvem Amazon S3</li>
   <li>Ele pode lidar com dados não estruturados, como fotos, vídeos, arquivos de log, backups e imagens de contêiner com (atualmente) o tamanho máximo de objeto suportado de 5 TB.</li>
   <li>Aqui está a explicação do projeto
       <ul>
         <li>Implemente o processo de upload de arquivo para o Minio</li>
         <li>Implemente o processo de download do arquivo do Minio</li>
         <li>Implemente o processo de adição de bucketname ao Minio</li>
         <li>Implemente o processo de listagem de todos os arquivos de bucketname no Minio</li>
         <li>Implemente o processo de listagem de todos os nomes de bucket no Minio</li>
         <li>Implementar o processo de exclusão de bucketname do Minio</li>
         <li>Implemente o processo de exclusão da lista de arquivos do bucketname do Minio</li>
         <li>Implementar o processo de exclusão de arquivo do bucketname do Minio</li>
         <li>Implementar o processo URL do objeto armazenado do bucketname do Minio</li>
       </ul>
   </li>
</ul>

### 🔨 Rodar App

<b>1 )</b> Instale o <b>Docker Desktop</b>. Aqui está o <b>link</b> de instalação: https://docs.docker.com/docker-for-windows/install/

<b>2 )</b> Abra o <b>Terminal</b> na pasta <b>resources</b> para executar o <b>Minio</b> no contêiner <b>Docker</b>

```
    docker-compose up -d
```
<b>3 )</b> Abra o <b>Minio</b> no navegador
```
    127.0.0.1:9001
```
<b>4 )</b> Logar no MINIO 
```
    username : minioadmin
    password : minioadmin
```
<b>5 )</b> Explore Rest APIs
<table style="width:100%">
  <tr>
    <th>Method</th>
    <th>Url</th>
    <th>Descrição</th>
    <th>Valid Request Body</th>
    <th>Valid Request Params</th>
  </tr>
  <tr>
    <td>POST</td>
    <td>/upload</td>
    <td>Upload do arquivo Minio</td>
    <td><a href="README.md#upload">Info</a></td>
    <td></td>
  </tr>
  <tr>
      <td>POST</td>
      <td>/addBucket/{bucketName}</td>
      <td>Adicionar BucketName no Minio</td>
      <td></td>
      <td><a href="README.md#addBucketName">Info</a></td>
  </tr>
  <tr>
      <td>GET</td>
      <td>/show/{bucketName}</td>
      <td>Mostrar Bucketname definido por seu nome no Minio</td>
      <td></td>
      <td><a href="README.md#showBucketName">Info</a></td>
  </tr>
  <tr>
      <td>GET</td>
      <td>/showBucketName</td>
      <td>Mostrar todos os BucketNames no Minio</td>
      <td></td>
      <td><a href="README.md#showAllBucketName">Info</a></td>
  </tr>
  <tr>
      <td>DELETE</td>
      <td>/removeBucket/{bucketName}</td>
      <td>Excluir bucketname definido do Minio</td>
      <td></td>
      <td><a href="README.md#deleteBucketName">Info</a></td>
  </tr>
  <tr>
       <td>DELETE</td>
       <td>/removeObject/{bucketName}/{objectName}</td>
       <td>Excluir objeto definido no nome do balde definido do Minio</td>
       <td></td>
       <td><a href="README.md#deleteObject">Info</a></td>
  </tr>
  <tr>
       <td>DELETE</td>
       <td>/removeListObject/{bucketName}</td>
       <td>Remova a lista de objetos no bucketname definido do Minio</td>
       <td><a href="README.md#deleteListObject">Info</a></td>
       <td></td>
  </tr>
  <tr>
       <td>GET</td>
       <td>/showListObjectNameAndDownloadUrl/{bucketName}</td>
       <td>Liste os nomes dos objetos e seu URL de download no bucketname definido do Minio</td>
       <td></td>
       <td><a href="README.md#objectInformation">Info</a></td>
  </tr>
  <tr>
       <td>GET</td>
       <td>/download/{bucketName}/{objectName}</td>
       <td>Baixar objeto em BucketName do Minio</td>
       <td></td>
       <td><a href="README.md#download">Info</a></td>
  </tr>
  <tr>
       <td>GET</td>
       <td>/showURL/{bucketName}/{objectName}</td>
       <td>Exibe URL objeto em BucketName do Minio</td>
       <td></td>
       <td><a href="README.md#showURL">Info</a></td>
  </tr>
</table>

### Dependências Usadas
* Spring Boot Web
* Minio
* Lombok
* AspectJ
* Apache Commons Lang
* Swagger

## Swagger
> **Acesso : http://localhost:8085/swagger-ui.html**

## Corpo de Solicitação Válido

##### <a id="upload">Upload -> http://localhost:8085/minio/upload</a>
```
    file : Uploaded File
    bucketname : commons
```

##### <a id="deleteListObject">Excluir uma lista de Objeto-> http://localhost:8085/minio/removeListObject/{bucketName}</a>
```
   [
       "de43ab54e89f4879a2baf87df1570f56.PNG",
       "f107737d21534f42a72dcf009a64a07d.PNG"
   ]
```

## Parâmetros de solicitação válidos

##### <a id="addBucketName">Adicionar nome do Bucket  -> http://localhost:8085/minio/addBucket/{bucketName}</a>
```
   http://localhost:8085/minio/addBucket/test1
```

##### <a id="showBucketName">Exibir nome do Bucket -> http://localhost:8085/minio/show/{bucketName}</a>
```
   http://localhost:8085/minio/show/commons
```

##### <a id="showAllBucketName">Mostrar todos os Bucket -> http://localhost:8085/minio/showBucketName</a>
```
   http://localhost:8085/minio/showBucketName
```

##### <a id="deleteBucketName">Excluir Bucket por nome -> http://localhost:8085/minio/removeBucket/{bucketName}</a>
```
   http://localhost:8085/minio/removeBucket/test1
```

##### <a id="objectInformation"> Liste os nomes dos objetos no Bucket -> http://localhost:8085/minio/showListObjectNameAndDownloadUrl/{bucketName}</a>
```
   http://localhost:8085/minio/showListObjectNameAndDownloadUrl/test1
```

##### <a id="download">Download do Arquivo -> http://localhost:8085/minio/download/{bucketName}/{objectName}</a>
```
   http://localhost:8085/minio/download/commons/ad94ff2e9b404772a1f9b98f4e11b4f9.PNG
```

##### <a id="deleteObject">Excluir objeto definido no nome do Bucket-> http://localhost:8085/minio/removeObject/{bucketName}/{objectName}</a>
```
   http://localhost:8085/minio/removeObject/commons/a2d203e188f94ccb8393e688deaf216a.jpg
```

##### <a id="showURL">Exibe URL do Objeto armazenado-> http://localhost:8085/minio/showURL/{bucketName}/{objectName}</a>
```
   http://localhost:8085/minio/showURL/commons/a2d203e188f94ccb8393e688deaf216a.jpg
```


