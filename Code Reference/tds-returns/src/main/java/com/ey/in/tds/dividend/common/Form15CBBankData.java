package com.ey.in.tds.dividend.common;

import org.springframework.stereotype.Component;

@Component
public class Form15CBBankData {
    public enum FORM_15CB_BANK_DATA {

        BankCode_1("Abhyudaya Cooperative Bank"),

        BankCode_2("Abu Dhabi Commercial Bank"),

        BankCode_3("Ahmedabad Mercantile Cooperative Bank"),

        BankCode_4("Akola Janata Commercial Cooperative Bank"),

        BankCode_5("Allahabad Bank"),

        BankCode_6("Almora Urban Co-Operative Bank ltd"),

        BankCode_7("Andhra Bank"),

        BankCode_8("Andhra Pragathi Grameena Bank"),

        BankCode_9("Apna Sahakari Bank Ltd"),

        BankCode_10("Australia and New Zealand Banking Group Ltd"),

        BankCode_11("Axis Bank"),

        BankCode_12("Bank Internasional Indonesia"),

        BankCode_13("Bank of America"),

        BankCode_14("Bank of Bahrein and Kuwait"),

        BankCode_15("Bank of Baroda"),

        BankCode_16("Bank of Ceylon"),

        BankCode_17("Bank of India"),

        BankCode_18("Bank of Maharashtra"),

        BankCode_19("MUFG Bank Ltd"),

        BankCode_20("Barclays Bank"),

        BankCode_21("Bassein Catholic Co-Op Bank Ltd"),

        BankCode_22("Bharat Cooperative Bank Mumbai Ltd"),

        BankCode_23("Bharatiya Mahila Bank Ltd"),

        BankCode_24("BNP Paribas Bank"),

        BankCode_25("Canara Bank"),

        BankCode_26("Capital Local Area Bank Ltd"),

        BankCode_27("Catholic Syrian Bank"),

        BankCode_28("Central Bank of India"),

        BankCode_29("Chinatrust Commercial Bank"),

        BankCode_30("CITI Bank"),

        BankCode_31("Citizen Credit Cooperative Bank"),

        BankCode_32("City Union Bank Ltd"),

        BankCode_33("Commonwealth Bank of Australia"),

        BankCode_34("Corporation Bank"),

        BankCode_35("Credit Agricole Corporate and Investment Bank"),

        BankCode_36("Credit Suisse AG"),

        BankCode_37("DBS Bank Ltd"),

        BankCode_38("DCB Bank Ltd"),

        BankCode_39("Dena Bank"),

        BankCode_40("Deposit Insurance and Credit Guarantee Corporation"),

        BankCode_41("Deutsche Bank"),

        BankCode_42("Dhanlaxmi Bank Ltd"),

        BankCode_43("DICGC"),

        BankCode_44("Doha Bank QSC"),

        BankCode_45("Dombivli Nagari Sahakari Bank Ltd"),

        BankCode_46("Export Import Bank of India"),

        BankCode_47("Federal Bank Ltd"),

        BankCode_48("Firstrand Bank Ltd"),

        BankCode_49("G P Parsik Bank"),

        BankCode_50("Gurgaon Gramin Bank Ltd"),

        BankCode_51("HDFC Bank Ltd"),

        BankCode_52("HSBC"),

        BankCode_53("HSBC Bank Oman Saog"),

        BankCode_54("ICICI Bank Ltd"),

        BankCode_55("IDBI Ltd"),

        BankCode_56("Indian Bank"),

        BankCode_57("Indian Overseas Bank"),

        BankCode_58("Indusind Bank Ltd"),

        BankCode_59("Industrial and Commercial Bank of China Ltd"),

        BankCode_60("ING Vysya Bank Ltd"),

        BankCode_61("Jalgaon Janata Sahkari Bank Ltd"),

        BankCode_62("Janakalyan Sahakari Bank Ltd"),

        BankCode_63("Janaseva Sahakari Bank (Borivli) Ltd"),

        BankCode_64("Janaseva Sahakari Bank Ltd"),

        BankCode_65("Janata Sahakari Bank Ltd (Pune)"),

        BankCode_66("Jankalyan Sahakari Bank Ltd"),

        BankCode_67("JP Morgan Chase Bank NA"),

        BankCode_68("Kallappanna Awade Ichalkaranji Janata Sahakari Bank Ltd"),

        BankCode_69("Kalupur Commercial Cooperative Bank"),

        BankCode_70("Kapol Cooperative Bank"),

        BankCode_71("Karnataka Bank Ltd"),

        BankCode_72("Karnataka Vikas Grameena Bank"),

        BankCode_73("Karur Vysya Bank"),

        BankCode_74("Kerala Gramin Bank"),

        BankCode_75("Kotak Mahindra Bank"),

        BankCode_76("Laxmi Vilas Bank"),

        BankCode_77("Mahanagar Cooperative Bank Ltd"),

        BankCode_78("Maharastra State Cooperative Bank"),

        BankCode_79("Mashreq Bank"),

        BankCode_80("Mizuho Corporate Bank Ltd"),

        BankCode_81("Nagar Urban Co-Operative Bank"),

        BankCode_82("Nagpur Nagrik Sahakari Bank Ltd"),

        BankCode_83("National Australia Bank"),

        BankCode_84("New India Cooperative Ban Ltd"),

        BankCode_85("NKGSB Cooperative Bank Ltd"),

        BankCode_86("North Malabar Gramin Bank"),

        BankCode_87("Nutan Nagarik Sahakari Bank Ltd"),

        BankCode_88("Oman International Bank"),

        BankCode_89("Oriental Bank of Commerce"),

        BankCode_90("Pragathi Krishna Gramin Bank"),

        BankCode_91("Prathama Bank"),

        BankCode_92("Prime Co-Operative Bank Ltd"),

        BankCode_93("Punjab and Maharashtra Cooperative Bank Ltd"),

        BankCode_94("Punjab and Sind Bank"),

        BankCode_95("Punjab National Bank"),

        BankCode_96("Rabobank International"),

        BankCode_97("Rajgurunagar Sahakari Bank Ltd"),

        BankCode_98("Rajkot Nagarik Sahakari Bank Ltd"),

        BankCode_99("Ratnakar Bank Ltd"),

        BankCode_100("Reserve Bank of India"),

        BankCode_101("Sahebrao Deshmukh Co-Op. Bank Ltd"),

        BankCode_102("SBER Bank"),

        BankCode_103("Shikshak Sahakari Bank Ltd"),

        BankCode_104("Shinhan Bank"),

        BankCode_105("Shri Chhatrapati Rajarshi Shahu Urban Co-Op Bank Ltd"),

        BankCode_106("Societe Generale"),

        BankCode_107("Solapur Janata Sahkari Bank Ltd"),

        BankCode_108("South Indian Bank"),

        BankCode_109("Standard Chartered Bank"),

        BankCode_110("State Bank of Bikaner and Jaipur"),

        BankCode_111("State Bank of Hyderabad"),

        BankCode_112("State Bank of India"),

        BankCode_113("SBM Bank (India) Limited"),

        BankCode_114("State Bank of Mysore"),

        BankCode_115("State Bank of Patiala"),

        BankCode_116("State Bank of Travancore"),

        BankCode_117("Sumitomo Mitsui Banking Corporation"),

        BankCode_118("Sutex Cooperative Bank Ltd"),

        BankCode_119("Syndicate Bank"),

        BankCode_120("Tamil Nadu Mercantile Bank"),

        BankCode_121("The A.P. Mahesh Co-Op Urban Bank Ltd"),

        BankCode_122("The Akola District Central Co-Operative Bank"),

        BankCode_123("The Andhra Pradesh State Coop Bank Ltd"),

        BankCode_124("The Bank of Nova Scotia"),

        BankCode_125("The Cosmos Cooperative Bank Ltd"),

        BankCode_126("The Delhi State Cooperative Bank Ltd"),

        BankCode_127("The Gadchiroli District Central Cooperative Bank Ltd"),

        BankCode_128("The Greater Bombay Co-operative Bank Ltd"),

        BankCode_129("The Gujarat State Co-Operative Bank Ltd"),

        BankCode_130("The Jalgaon Peoples Co-Op Bank"),

        BankCode_131("The Jammu and Kashmir Bank"),

        BankCode_132("The Kalyan Janata Sahakari Bank Ltd"),

        BankCode_133("The Kangra Central Cooperative Bank Ltd"),

        BankCode_134("The Kangra Cooperative Bank Ltd"),

        BankCode_135("The Karad Urban Co-op Bank Ltd"),

        BankCode_136("The Karnataka State Apex Cooperative Bank Ltd"),

        BankCode_137("The Kurmanchal Nagar Sahkari Bank Ltd"),

        BankCode_138("The Mehsana Urban Cooperative Bank Ltd"),

        BankCode_139("The Mumbai District Central Co-Op Bank Ltd"),

        BankCode_140("The Municipal Co Operative Bank Ltd, Mumbai"),

        BankCode_141("The Nainital Bank Ltd"),

        BankCode_142("The Nasik Merchants Co-Op Bank Ltd"),

        BankCode_143("The Rajasthan State Cooperative Bank Ltd"),

        BankCode_144("The Royal Bank of Scotland N.V."),

        BankCode_145("The Saraswat Cooperative Bank Ltd"),

        BankCode_146("The Seva Vikas Co-Operative Bank Ltd"),

        BankCode_147("The Shamrao Vithal Cooperative Bank Ltd"),

        BankCode_148("The Surat District Co Operative Bank Ltd"),

        BankCode_149("The Surat Peoples Co-Op Bank Ltd"),

        BankCode_150("The Tamilnadu State Apex Cooperative Bank"),

        BankCode_151("The Thane Bharat Sahakari Bank Ltd"),

        BankCode_152("The Thane District Central Co-Op Bank Ltd"),

        BankCode_153("The Varachha Co-Op. Bank Ltd"),

        BankCode_154("The Vishweshwar Sahakari Bank Ltd"),

        BankCode_155("The West Bengal State Cooperative Bank Ltd"),

        BankCode_156("The Zoroastrian Cooperative Bank Limited"),

        BankCode_157("TJSB Sahakari Bank Ltd"),

        BankCode_158("Tumkur Grain Merchants Cooperative Bank Ltd"),

        BankCode_159("UCO Bank"),

        BankCode_160("Union Bank of India"),

        BankCode_161("United Bank of India"),

        BankCode_162("United Overseas Bank"),

        BankCode_163("Vasai Vikas Sahakari Bank Ltd"),

        BankCode_164("Vijaya Bank"),

        BankCode_165("Westpac Banking Corporation"),

        BankCode_166("Woori Bank"),

        BankCode_167("Yes Bank Ltd"),

        BankCode_168("Zila Sahkari Bank Ltd Ghaziabad"),

        BankCode_169("The HASTI Co-Operative Bank Ltd"),

        BankCode_170("Bandhan Bank Limited"),

        BankCode_171("IDFC Bank Limited"),

        BankCode_172("Industrial Bank of Korea"),

        BankCode_173("National Bank of Abu Dhabi PJSC"),

        BankCode_174("Surat National Cooperative Bank Limited"),

        BankCode_999("Other Bank");

        private String bankName;

        FORM_15CB_BANK_DATA (String bankName) {
            this.bankName = bankName;
        }
    }
    public String getBankCodeValue(String bankName){
        if (bankName != null && !bankName.isEmpty()) {
            for (FORM_15CB_BANK_DATA s : FORM_15CB_BANK_DATA.values()) {
                String enumBankName = s.bankName.replaceAll("\\s", "");
                String bankNameAfterAlteration = bankName.replaceAll("\\s", "");
                if (enumBankName.equalsIgnoreCase(bankNameAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_999";
    }
}
