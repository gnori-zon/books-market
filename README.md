# books-market

This is a rest-api for a database with a library domain. This api uses jwt

<p align="center">
   <img src="https://img.shields.io/badge/Version-1.0-important" alt="App Version">
   <img src="https://img.shields.io/badge/Lecense-MIT-9cf" alt="License">
</p>

### About
The subject area is presented below:
- Book
- Publisher
- Author
- Genre
- Review
- Binary content
- User (for security)

![image](https://user-images.githubusercontent.com/108410527/231844395-bff6aa61-ff26-48d8-bd10-beec3d7245c6.png)


 
<details>
   <summary>Generate schema</summary>
   
```sql
   create table public._user
   (
      id bigint not null
         primary key,
      password varchar(255),
      role varchar(255),
      username varchar(255)
         constraint uk_nlcolwbx8ujaen5h0u2kr2bn2
            unique
   );

   create table public.author
   (
      id bigint not null
         primary key,
      first_name varchar(255),
      last_name varchar(255)
   );

   create table public.genre
   (
      id bigint not null
         primary key,
      name varchar(255)
         constraint uk_ctffrbu4484ft8dlsa5vmqdka
            unique
   );

   create table public.publisher
   (
      id bigint not null
         primary key,
      name varchar(255)
   );

   create table public.book
   (
      id bigint not null
         primary key,
      description varchar(255),
      language smallint,
      name varchar(255),
      release_date timestamp(6),
      publisher_id bigint
         constraint fkgtvt7p649s4x80y6f4842pnfq
            references public.publisher
   );

   create table public.binary_content
   (
      id bigint not null
         primary key,
      image bytea,
      raw bytea,
      size_raw double precision,
      type_image varchar(255),
      type_raw varchar(255),
      book_id bigint
         constraint fkbkdiikpriqq74wbuh7tbn6kk3
            references public.book
   );

   create table public.book_author
   (
      book_id bigint not null
         constraint fkhwgu59n9o80xv75plf9ggj7xn
            references public.book,
      author_id bigint not null
         constraint fkbjqhp85wjv8vpr0beygh6jsgo
            references public.author
   );

   create table public.book_genre
   (
      book_id bigint not null
         constraint fk52evq6pdc5ypanf41bij5u218
            references public.book,
      genre_id bigint not null
         constraint fk8l6ops8exmjrlr89hmfow4mmo
            references public.genre
   );

   create table public.publisher_authors
   (
      publisher_entity_id bigint not null
         constraint fk20nedfiowh877o7u7up022hsd
            references public.publisher,
      authors_id bigint not null
         constraint fk5mejuoy7relanuqylkpcj0uc3
            references public.author
   );

   create table public.review
   (
      id bigint not null
         primary key,
      book_id bigint,
      content varchar(255),
      title varchar(255)
   );
```
   
</details>

## Configuration
In yaml file you can:
 - specify the port
 ```yaml
server:
  port: 
  ```
- specify this database
```yaml
spring:
  datasource:
    url: 
    username: 
    password: 
```
- change the generation mode of sql code for creating and deleting data ex: create-drop or (create, update, validate, none)
```yaml
    hibernate.ddl-auto: 
```
- change the maximum size of processed files
```yaml
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
```
- specify the secret key with which the jwt will be created jwt
```yaml
  jwt:
    secret: 
```

## Endpoints
Postman collection : <a href="https://github.com/gnori-zon/books-market/blob/master/books-market.postman_collection.json">Collection</a>

<details>
   <summary>Requests</summary>
   
   <img src="https://user-images.githubusercontent.com/108410527/231857829-43271a64-ea38-4876-a38f-78598e70bce7.png">
   <img src="https://user-images.githubusercontent.com/108410527/231860535-4c4f1e70-7078-4c23-8e03-85cac149cca0.png">
   <img src="https://user-images.githubusercontent.com/108410527/231860611-909897ac-813c-4c65-936f-b247522c31cf.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858015-5cdb0774-9ca8-4d3c-8603-d0b8316d654b.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858191-4ce5d590-e50c-42db-b2ff-3a930feb2740.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858250-8eac1441-49db-4180-a566-5de5d174b155.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858322-7465d6ad-0db6-4cbb-b776-7f5dfd579f24.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858358-debb23d2-6b46-47c0-a2f7-5a862063c95b.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858385-67474ff9-089e-4eba-ad79-3e74798099b7.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858413-522ee102-3b1b-4ef4-a88c-25f9ab90daa6.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858452-8ebc65c9-d2e5-47a2-ac8b-1cc7f30a2542.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858498-6fb17fa6-3c66-4c78-8389-86a1a7525a44.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858521-f63878e9-4990-42d1-b4ab-faf05c947a3e.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858568-6676cd64-c26e-4921-b61d-1af3524e0b09.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858609-c53f5aeb-b3b9-4c17-917b-06346b116f95.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858631-55b949e9-09de-4319-a4b0-340cbcc51a97.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858657-941999b2-7845-44bd-a2f9-116642241419.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858691-30450e09-babe-4efd-8a5b-a1a3b5f41a8c.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858754-7363692c-ca18-47f0-ab77-dfe5ee4d058b.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858717-a7fa6444-e60b-4cf9-8527-14e58aaba16f.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858820-516caf51-560e-4ddc-852e-e079acb23100.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858852-7245aa20-4c83-4df6-af47-d9e7ef2cbc20.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858892-687808e9-f91a-44ec-9e02-0fffd9b036a0.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858936-3637367a-236e-41d7-8d65-a5db54f00f8b.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858971-f1b8d926-95da-45cb-b7ea-ec9f00da3d39.png">
   <img src="https://user-images.githubusercontent.com/108410527/231858993-765bf2aa-2dc6-475a-b6f0-56a8f7cca0fe.png">

</details>

## Developers

- [gnori-zon](https://github.com/gnori-zon)

