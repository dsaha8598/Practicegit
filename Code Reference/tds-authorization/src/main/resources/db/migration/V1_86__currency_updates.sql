ALTER TABLE tds.currency ADD
cross_selling_rate decimal,
cross_buying_rate decimal,
buying_tt decimal,
buying_bill decimal,
selling_tt decimal,
selling_bill decimal,
Date [datetime];

alter table tds.currency drop column symbol;
alter table tds.currency drop column currency_value;
alter table tds.currency drop column country_name;
