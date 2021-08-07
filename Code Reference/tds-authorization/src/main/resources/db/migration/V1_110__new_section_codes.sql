insert into tds.filing_section_code(section_name,section_code) select '194K','94K' where not exists (select 1 from tds.filing_section_code where section_code= '94K');
