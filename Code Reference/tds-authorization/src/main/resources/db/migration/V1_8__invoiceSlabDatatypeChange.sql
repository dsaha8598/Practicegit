alter table tds.basis_of_cess_details 
drop constraint [DF__basis_of___invoi__0A9D95DB];

alter table tds.basis_of_cess_details 
drop constraint [DF__basis_of___invoi__0B91BA14];

alter table tds.basis_of_cess_details 
drop COLUMN invoice_slab_from;

alter table tds.basis_of_cess_details 
drop COLUMN invoice_slab_to;

alter table tds.basis_of_cess_details 
add invoice_slab_from bigint;

alter table tds.basis_of_cess_details 
add invoice_slab_to bigint;

alter table tds.basis_of_surcharge_details 
drop COLUMN invoice_slab_from;

alter table tds.basis_of_surcharge_details 
drop COLUMN invoice_slab_to;

alter table tds.basis_of_surcharge_details 
add invoice_slab_from bigint;

alter table tds.basis_of_surcharge_details 
add invoice_slab_to bigint;

