CREATE TABLE tds.classification
(id bigint primary key identity(1,1),
classification_name varchar(45),
classification_code varchar(45),
applicability_for_clause_34a varchar(10),
active smallint DEFAULT 1,
created_by varchar(256) DEFAULT 'EY Admin',
modified_by varchar(256) DEFAULT 'EY Admin',
created_date datetime DEFAULT GETDATE(),
modified_date datetime DEFAULT GETDATE()
);

INSERT INTO tds.classification (classification_code, classification_name,applicability_for_clause_34a)
VALUES('MATERIALS','Materials','no'),('SERVICES','Services','yes'),('HYBRID','Hybrid','yes'),('PAYROLL','Payroll','no'),
      ('OTHERS', 'Others','no');
      