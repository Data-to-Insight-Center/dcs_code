---
--- Copyright 2013 Johns Hopkins University
---
--- Licensed under the Apache License, Version 2.0 (the "License");
--- you may not use this file except in compliance with the License.
--- You may obtain a copy of the License at
---
---     http://www.apache.org/licenses/LICENSE-2.0
---
--- Unless required by applicable law or agreed to in writing, software
--- distributed under the License is distributed on an "AS IS" BASIS,
--- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--- See the License for the specific language governing permissions and
--- limitations under the License.
---
CREATE SCHEMA `dcslite` DEFAULT CHARACTER SET utf8; \g
USE dcslite;
CREATE TABLE `users` (`id` INT(11) NOT NULL AUTO_INCREMENT,`username` VARCHAR(1024) NOT NULL,`password` VARCHAR(1024) NOT NULL,`app_key` VARCHAR(1024) NULL DEFAULT NULL,`app_secret` VARCHAR(1024) NULL DEFAULT NULL,`first_name` VARCHAR(1024) NULL DEFAULT NULL,`last_name` VARCHAR(1024) NULL DEFAULT NULL,`occupation` VARCHAR(1024) NULL DEFAULT NULL,`organization` VARCHAR(1024) NULL DEFAULT NULL,`secret_question` VARCHAR(1024) NULL DEFAULT NULL,`secret_answer` VARCHAR(1024) NULL DEFAULT NULL,`email` VARCHAR(1024) NULL DEFAULT NULL,`sid` VARCHAR(1024) NULL DEFAULT NULL,PRIMARY KEY (`id`) );
