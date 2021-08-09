update tds.filing_section_code set section_name='194IA' where section_code='4IA';
update tds.filing_section_code set section_name='194IB' where section_code='4IB';
​
insert into tds.filing_section_code(section_name,section_code) select '194N','94N' where not exists (select 1 from tds.filing_section_code where section_code= '94N');
