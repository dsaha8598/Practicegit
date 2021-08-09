package com.ey.in.tds.dividend.common;

import org.springframework.stereotype.Component;

@Component
public class Form15CAData {

    public enum FORM_15CA_CURRENCY_DATA {

        Currency_1("AFGAHANI"),

        Currency_2("ALBANIAN LEK"),

        Currency_3("ALGERIAN DINAR"),

        Currency_4("ANDORAN PESTA"),

        Currency_5("ANGOLAN NEW KWANZA"),

        Currency_6("ARGENTINE PESOS"),

        Currency_7("ARMENIAN DRAM"),

        Currency_8("ARUBAN GUILDER"),

        Currency_9("AUSTRALIAN DOLLAR"),

        Currency_10("AUSTRIAN SCHILLING"),

        Currency_11("AZERBAIJAN MANAT"),

        Currency_12("BAHAMIAN DOLLAR"),

        Currency_13("BAHRAINI DINAR"),

        Currency_14("BANGLADESH TAKA"),

        Currency_15("BARBADOS DOLLAR"),

        Currency_16("BELARUSSIAN RUBLE"),

        Currency_17("BELGIAN FRANC"),

        Currency_18("BELIZE DOLLAR"),

        Currency_19("BERMUDIAN DOLLAR"),

        Currency_20("BHUTAN NGULTRUM"),

        Currency_21("BOLIVIAN BOLIVIANO"),

        Currency_22("BOTSWANA PULA"),

        Currency_23("BRAZILIAN REAL"),

        Currency_24("BRUNEI DOLLAR"),

        Currency_25("BULGARIAN LEV"),

        Currency_26("BURUNDI FRANC"),

        Currency_27("CAMBODIAN REIL"),

        Currency_28("CANADIAN DOLLAR"),

        Currency_29("CAPE VERDE ESCUDO"),

        Currency_30("CAYMAN ISLANDS DOLLAR"),

        Currency_31("CFP FRANCS"),

        Currency_32("CHILEAN PESO"),

        Currency_33("COLOMBIAN PESO"),

        Currency_34("COMOROS FRANC"),

        Currency_35("COSTA RICAN COLON"),

        Currency_36("CROATIA KUNA"),

        Currency_37("CUBAN PESO"),

        Currency_38("CYPRUS POUND"),

        Currency_39("DANISH KRONE"),

        Currency_40("DEUTSCH MARK"),

        Currency_41("DJIBOUTI FRANC"),

        Currency_42("DOBRA"),

        Currency_43("DOMINICAN PESO"),

        Currency_44("EAST CARRIBEAN DOLLAR"),

        Currency_45("ECUADOR SUCRE"),

        Currency_46("EGYPTIAN POUND"),

        Currency_47("EL SALVADOR COLON"),

        Currency_48("ESTONIAN KROON"),

        Currency_49("ETHOPIAN BIRR"),

        Currency_50("EURO"),

        Currency_51("FALKLAND ISLANDSPOUND"),

        Currency_52("FIJI DOLLAR"),

        Currency_53("FINISH MARKKA"),

        Currency_54("FRENCH FRANC"),

        Currency_55("GAMBIAN DALASI"),

        Currency_56("GEORGIAN LARI"),

        Currency_57("GHANA CEDI"),

        Currency_58("GIBRALTAR POUND"),

        Currency_59("GREEK DRACHMA"),

        Currency_60("GUATEMALA QUETZAL"),

        Currency_61("GUINEA FRANC"),

        Currency_62("GUINEA-BISSAU PESO"),

        Currency_63("GUYANA DOLLAR"),

        Currency_64("HAITI GOURDE"),

        Currency_65("HONDURAS LEMPIRA"),

        Currency_66("HONGKONG DOLLAR"),

        Currency_67("HRYVNIA"),

        Currency_68("HUNGARIAN FORINT"),

        Currency_69("ICELAND KRONA"),

        Currency_70("INDIAN RUPEE"),

        Currency_71("INDONESIAN RUPIAH"),

        Currency_72("IRANIAN RIAL"),

        Currency_73("IRAQI DINAR"),

        Currency_74("IRISH POUNDS"),

        Currency_75("ISRAELI SHEKEL"),

        Currency_76("ITALIAN LIRA"),

        Currency_77("JAMAICAN DOLLAR"),

        Currency_78("JAPANESE YEN"),

        Currency_79("JORDANIAN DINAR"),

        Currency_80("KAZAKSTAN TENGE"),

        Currency_81("KENYAN SHILLING"),

        Currency_82("KINA"),

        Currency_83("KORUNA"),

        Currency_84("KUWAITI DINAR"),

        Currency_85("KWACHA"),

        Currency_86("KYAT"),

        Currency_87("KYRGYZSTAN SOM"),

        Currency_88("LAOS KIP"),

        Currency_89("LATVIAN LAT"),

        Currency_90("LEBANESE POUND"),

        Currency_91("LEONE"),

        Currency_92("LESOTHO LOTI"),

        Currency_93("LIBERIAN DOLLAR"),

        Currency_94("LIBYAN DINAR"),

        Currency_95("LILANGENI"),

        Currency_96("LITHUANIAN LITAS"),

        Currency_97("LUXEMBOURG FRANCS"),

        Currency_98("MACAU PATACA"),

        Currency_99("OTHERS"),

        Currency_100("MACEDONIAN DENAR"),

        Currency_101("MALAGASSY FRANC"),

        Currency_102("MALAWIAN KWACHA"),

        Currency_103("MALAYSIAN RINGGIT"),

        Currency_104("MALDIVE RUFIYAA"),

        Currency_105("MALTESE LIRA"),

        Currency_106("MAURITANIAN OUGUIYA"),

        Currency_107("MAURITIUS RUPEE"),

        Currency_108("MEXICAN PESO"),

        Currency_109("MOLDOVAN LEU"),

        Currency_110("MONGOLIAN TUGRIK"),

        Currency_111("MORACCAN DIRHAM"),

        Currency_112("MOZAMBIQUE METICAL"),

        Currency_113("NAMIBIA DOLLAR"),

        Currency_114("NEPALESE RUPEE"),

        Currency_115("NETHERLAND ANTILLIAN GUILDER"),

        Currency_116("NETHERLANDS GUILDER"),

        Currency_117("NEW DINAR"),

        Currency_118("NEW TAIWAN DOLLAR"),

        Currency_119("NEW ZEALAND DOLLAR"),

        Currency_120("NICARAGUAN CORDOBA ORO"),

        Currency_121("NIGERIAN NAIRA"),

        Currency_122("NORTH KOREAN WON"),

        Currency_123("NORWEGIAN KRONES"),

        Currency_124("PAKISTANI RUPEE"),

        Currency_125("PANAMAN BALBOA"),

        Currency_126("PARAGUAY GUARANI"),

        Currency_127("PARANGA"),

        Currency_128("PERUVIAN NUEVO SOL"),

        Currency_129("PESO"),

        Currency_130("PHILLIPINES PESOS"),

        Currency_131("POLISH ZLOTY"),

        Currency_132("PORTUGUESE ESCUDO"),

        Currency_133("POUND STERLING"),

        Currency_134("QATARI RIAL"),

        Currency_135("RAND"),

        Currency_136("RIAL OMANIS"),

        Currency_137("ROMANIAN LEU"),

        Currency_138("ROUBLE"),

        Currency_139("RWANDA FRANC"),

        Currency_140("SAUDI RIYAL"),

        Currency_141("SEYCHELLES RUPEE"),

        Currency_142("SINGAPORE DOLLAR"),

        Currency_143("SLOVAKI KORUNA"),

        Currency_144("SLOVENIAN TOLAR"),

        Currency_145("SOLOMAN ISLANDS DOLLAR"),

        Currency_146("SOMALI SHILLING"),

        Currency_147("SOUTH KOREAN WON"),

        Currency_148("SPANISH PESETA"),

        Currency_149("SRI LANKA RUPEE"),

        Currency_150("ST. HELENA POUND"),

        Currency_151("SUDANESE DINAR"),

        Currency_152("SURINAME GUILDER"),

        Currency_153("SWEDISH KRONA"),

        Currency_154("SWISS FRANC"),

        Currency_155("SYRIAN POUND"),

        Currency_156("TAJIK ROUBLE"),

        Currency_157("TALA"),

        Currency_158("TANZANIAN SCHILLING"),

        Currency_159("THAI BAHTS"),

        Currency_160("TIMOR ESCUDO"),

        Currency_161("TRINIDAD & TOBAGO DOLLAR"),

        Currency_162("TURKEMENI MANAT"),

        Currency_163("TURKISH LIRA"),

        Currency_164("TURNISIAN DINAR"),

        Currency_165("UAE DIRHAM"),

        Currency_166("UGANDA SHILLING"),

        Currency_167("US DOLLAR"),

        Currency_168("UZBEKISTAN SUM"),

        Currency_169("VATU"),

        Currency_170("VENEZUELAN BOLIVAR"),

        Currency_171("VIETNAM DONG"),

        Currency_172("YEMENI RIAL"),

        Currency_173("YUAN RENMINBI"),

        Currency_174("ZAIRE"),

        Currency_175("ZIMBABWE DOLLAR"),

        Currency_176("CFA FRANC");

        private String currency;

        FORM_15CA_CURRENCY_DATA (String currency) {
            this.currency = currency;
        }
    }

    public enum FORM_15CA_STATE_DATA {
        StateCode_01("Andaman and Nicobar islands"),
        StateCode_02("Andhra Pradesh"),
        StateCode_03("Arunachal Pradesh"),
        StateCode_04("Assam"),
        StateCode_05("Bihar"),
        StateCode_06("Chandigarh"),
        StateCode_07("Dadra Nagar and Haveli"),
        StateCode_08("Daman and Diu"),
        StateCode_09("Delhi"),
        StateCode_10("Goa"),
        StateCode_11("Gujarat"),
        StateCode_12("Haryana"),
        StateCode_13("Himachal Pradesh"),
        StateCode_14("Jammu and Kashmir"),
        StateCode_15("Karnataka"),
        StateCode_16("Kerala"),
        StateCode_17("Lakshadweep"),
        StateCode_18("Madhya Pradesh"),
        StateCode_19("Maharashtra"),
        StateCode_20("Manipur"),
        StateCode_21("Meghalaya"),
        StateCode_22("Mizoram"),
        StateCode_23("Nagaland"),
        StateCode_24("Orissa"),
        StateCode_25("Pondicherr"),
        StateCode_26("Punjab"),
        StateCode_27("Rajasthan"),
        StateCode_28("Sikkim"),
        StateCode_29("Tamil Nadu"),
        StateCode_30("Tripura"),
        StateCode_31("Uttar Pradesh"),
        StateCode_32("West Bengal"),
        StateCode_33("Chattisgarh"),
        StateCode_34("Uttarakhand"),
        StateCode_35("Jharkhand"),
        StateCode_36("Telangana"),
        StateCode_37("Ladakh"),
        StateCode_99("Foreign");

        private String stateCode;

        FORM_15CA_STATE_DATA (String stateCode) {
            this.stateCode = stateCode;
        }
    }

    public enum FORM_15CA_COUNTRY_DATA {

        CountryCode_1("CANADA"),
        CountryCode_1001("�LAND ISLANDS"),
        CountryCode_1002("BONAIRE, SINT EUSTATIUS AND SABA"),
        CountryCode_1003("BOUVET ISLAND"),
        CountryCode_1004("FRENCH SOUTHERN TERRITORIES"),
        CountryCode_1005("HEARD ISLAND AND MCDONALD ISLANDS"),
        CountryCode_1006("SAINT BARTH�LEMY"),
        CountryCode_1007("SAINT MARTIN (FRENCH PART)"),
        CountryCode_1008("SOUTH GEORGIA AND THE SOUTH SANDWICH ISLANDS"),
        CountryCode_1009("UNITED STATES MINOR OUTLYING ISLANDS"),
        CountryCode_1010("ANTARCTICA"),
        CountryCode_1011("PITCAIRN"),
        CountryCode_1012("SVALBARD AND JAN MAYEN"),
        CountryCode_1013("WESTERN SAHARA"),
        CountryCode_1014("BRITISH INDIAN OCEAN TERRITORY"),
        CountryCode_1015("CURA�AO"),
        CountryCode_1242("BAHAMAS"),
        CountryCode_1246("BARBADOS"),
        CountryCode_1264("ANGUILLA"),
        CountryCode_1268("ANTIGUA AND BARBUDA"),
        CountryCode_1284("VIRGIN ISLANDS (BRITISH)"),
        CountryCode_1340("VIRGIN ISLANDS (U.S.)"),
        CountryCode_1345("CAYMAN ISLANDS"),
        CountryCode_14("PORTUGAL"),
        CountryCode_1441("BERMUDA"),
        CountryCode_1473("GRENADA"),
        CountryCode_1481("GUERNSEY"),
        CountryCode_15("NORFOLK ISLAND"),
        CountryCode_1534("JERSEY"),
        CountryCode_1624("ISLE OF MAN"),
        CountryCode_1649("TURKS AND CAICOS ISLANDS"),
        CountryCode_1664("MONTSERRAT"),
        CountryCode_1670("NORTHERN MARIANA ISLANDS"),
        CountryCode_1671("GUAM"),
        CountryCode_1721("SINT MAARTEN (DUTCH PART)"),
        CountryCode_1758("SAINT LUCIA"),
        CountryCode_1767("DOMINICA"),
        CountryCode_1784("SAINT VINCENT AND THE GRENADINES"),
        CountryCode_1787("PUERTO RICO"),
        CountryCode_1809("DOMINICAN REPUBLIC"),
        CountryCode_1868("TRINIDAD AND TOBAGO"),
        CountryCode_1869("SAINT KITTS AND NEVIS"),
        CountryCode_1876("JAMAICA"),
        CountryCode_2("USA"),
        CountryCode_20("EGYPT"),
        CountryCode_211("SOUTH SUDAN"),
        CountryCode_212("MOROCCO"),
        CountryCode_213("ALGERIA"),
        CountryCode_216("TUNISIA"),
        CountryCode_218("LIBYA"),
        CountryCode_220("GAMBIA"),
        CountryCode_221("SENEGAL"),
        CountryCode_222("MAURITANIA"),
        CountryCode_223("MALI"),
        CountryCode_224("GUINEA"),
        CountryCode_225("C�TE D'IVOIRE"),
        CountryCode_226("BURKINA FASO"),
        CountryCode_227("NIGER"),
        CountryCode_228("TOGO"),
        CountryCode_229("BENIN"),
        CountryCode_230("MAURITIUS"),
        CountryCode_231("LIBERIA"),
        CountryCode_232("SIERRA LEONE"),
        CountryCode_233("GHANA"),
        CountryCode_234("NIGERIA"),
        CountryCode_235("CHAD"),
        CountryCode_236("CENTRAL AFRICAN REPUBLIC"),
        CountryCode_237("CAMEROON"),
        CountryCode_238("CABO VERDE"),
        CountryCode_239("SAO TOME AND PRINCIPE"),
        CountryCode_240("EQUATORIAL GUINEA"),
        CountryCode_241("GABON"),
        CountryCode_242("CONGO"),
        CountryCode_243("CONGO (DEM. REP.)"),
        CountryCode_244("ANGOLA"),
        CountryCode_245("GUINEA-BISSAU"),
        CountryCode_248("SEYCHELLES"),
        CountryCode_249("SUDAN"),
        CountryCode_250("RWANDA"),
        CountryCode_251("ETHIOPIA"),
        CountryCode_252("SOMALIA"),
        CountryCode_253("DJIBOUTI"),
        CountryCode_254("KENYA"),
        CountryCode_255("TANZANIA"),
        CountryCode_256("UGANDA"),
        CountryCode_257("BURUNDI"),
        CountryCode_258("MOZAMBIQUE"),
        CountryCode_260("ZAMBIA"),
        CountryCode_261("MADAGASCAR"),
        CountryCode_262("R�UNION"),
        CountryCode_263("ZIMBABWE"),
        CountryCode_264("NAMIBIA"),
        CountryCode_265("MALAWI"),
        CountryCode_266("LESOTHO"),
        CountryCode_267("BOTSWANA"),
        CountryCode_268("SWAZILAND"),
        CountryCode_269("MAYOTTE"),
        CountryCode_270("COMOROS"),
        CountryCode_28("SOUTH AFRICA"),
        CountryCode_290("SAINT HELENA"),
        CountryCode_291("ERITREA"),
        CountryCode_297("ARUBA"),
        CountryCode_298("FAROE ISLANDS"),
        CountryCode_299("GREENLAND"),
        CountryCode_30("GREECE"),
        CountryCode_31("NETHERLANDS"),
        CountryCode_32("BELGIUM"),
        CountryCode_33("FRANCE"),
        CountryCode_35("SPAIN"),
        CountryCode_350("GIBRALTAR"),
        CountryCode_352("LUXEMBOURG"),
        CountryCode_353("IRELAND"),
        CountryCode_354("ICELAND"),
        CountryCode_355("ALBANIA"),
        CountryCode_356("MALTA"),
        CountryCode_357("CYPRUS"),
        CountryCode_358("FINLAND"),
        CountryCode_359("BULGARIA"),
        CountryCode_36("HUNGARY"),
        CountryCode_370("LITHUANIA"),
        CountryCode_371("LATVIA"),
        CountryCode_372("ESTONIA"),
        CountryCode_373("MOLDOVA"),
        CountryCode_374("ARMENIA"),
        CountryCode_375("BELARUS"),
        CountryCode_376("ANDORRA"),
        CountryCode_377("MONACO"),
        CountryCode_378("SAN MARINO"),
        CountryCode_380("UKRAINE"),
        CountryCode_381("SERBIA"),
        CountryCode_382("MONTENEGRO"),
        CountryCode_385("CROATIA"),
        CountryCode_386("SLOVENIA"),
        CountryCode_387("BOSNIA AND HERZEGOVINA"),
        CountryCode_389("MACEDONIA"),
        CountryCode_40("ROMANIA"),
        CountryCode_41("SWITZERLAND"),
        CountryCode_420("CZECH REPUBLIC"),
        CountryCode_421("SLOVAKIA"),
        CountryCode_423("LIECHTENSTEIN"),
        CountryCode_43("AUSTRIA"),
        CountryCode_44("UNITED KINGDOM"),
        CountryCode_45("DENMARK"),
        CountryCode_46("SWEDEN"),
        CountryCode_47("NORWAY"),
        CountryCode_48("POLAND"),
        CountryCode_49("GERMANY"),
        CountryCode_5("ITALY"),
        CountryCode_500("FALKLAND ISLANDS"),
        CountryCode_501("BELIZE"),
        CountryCode_502("GUATEMALA"),
        CountryCode_503("EL SALVADOR"),
        CountryCode_504("HONDURAS"),
        CountryCode_505("NICARAGUA"),
        CountryCode_506("COSTA RICA"),
        CountryCode_507("PANAMA"),
        CountryCode_508("SAINT PIERRE AND MIQUELON"),
        CountryCode_509("HAITI"),
        CountryCode_51("PERU"),
        CountryCode_52("MEXICO"),
        CountryCode_53("CUBA"),
        CountryCode_54("ARGENTINA"),
        CountryCode_55("BRAZIL"),
        CountryCode_56("CHILE"),
        CountryCode_57("COLOMBIA"),
        CountryCode_58("VENEZUELA"),
        CountryCode_590("GUADELOUPE"),
        CountryCode_591("BOLIVIA"),
        CountryCode_592("GUYANA"),
        CountryCode_593("ECUADOR"),
        CountryCode_594("FRENCH GUIANA"),
        CountryCode_595("PARAGUAY"),
        CountryCode_596("MARTINIQUE"),
        CountryCode_597("SURINAME"),
        CountryCode_598("URUGUAY"),
        CountryCode_6("HOLY SEE"),
        CountryCode_60("MALAYSIA"),
        CountryCode_61("AUSTRALIA"),
        CountryCode_62("INDONESIA"),
        CountryCode_63("PHILIPPINES"),
        CountryCode_64("NEW ZEALAND"),
        CountryCode_65("SINGAPORE"),
        CountryCode_66("THAILAND"),
        CountryCode_670("EAST TIMOR"),
        CountryCode_672("COCOS (KEELING) ISLANDS"),
        CountryCode_673("BRUNEI"),
        CountryCode_674("NAURU"),
        CountryCode_675("PAPUA NEW GUINEA"),
        CountryCode_676("TONGA"),
        CountryCode_677("SOLOMON ISLANDS"),
        CountryCode_678("VANUATU"),
        CountryCode_679("FIJI ISLANDS"),
        CountryCode_680("PALAU"),
        CountryCode_681("WALLIS AND FUTUNA"),
        CountryCode_682("COOK ISLANDS"),
        CountryCode_683("NIUE"),
        CountryCode_684("AMERICAN SAMOA"),
        CountryCode_685("SAMOA"),
        CountryCode_686("KIRIBATI"),
        CountryCode_687("NEW CALEDONIA"),
        CountryCode_688("TUVALU"),
        CountryCode_689("FRENCH POLYNESIA"),
        CountryCode_690("TOKELAU"),
        CountryCode_691("MICRONESIA"),
        CountryCode_692("MARSHALL ISLANDS"),
        CountryCode_7("KAZAKHSTAN"),
        CountryCode_8("RUSSIA"),
        CountryCode_81("JAPAN"),
        CountryCode_82("KOREA (SOUTH)"),
        CountryCode_84("VIET NAM"),
        CountryCode_850("KOREA (NORTH)"),
        CountryCode_852("HONG KONG"),
        CountryCode_853("MACAO"),
        CountryCode_855("CAMBODIA"),
        CountryCode_856("LAO PEOPLE'S DEMOCRATIC REPUBLIC"),
        CountryCode_86("CHINA"),
        CountryCode_880("BANGLADESH"),
        CountryCode_886("TAIWAN"),
        CountryCode_9("CHRISTMAS ISLAND"),
        CountryCode_90("TURKEY"),
        CountryCode_91("INDIA"),
        CountryCode_92("PAKISTAN"),
        CountryCode_93("AFGHANISTAN"),
        CountryCode_94("SRI LANKA"),
        CountryCode_95("MYANMAR"),
        CountryCode_960("MALDIVES"),
        CountryCode_961("LEBANON"),
        CountryCode_962("JORDAN"),
        CountryCode_963("SYRIA"),
        CountryCode_964("IRAQ"),
        CountryCode_965("KUWAIT"),
        CountryCode_966("SAUDI ARABIA"),
        CountryCode_967("YEMEN"),
        CountryCode_968("OMAN"),
        CountryCode_970("PALESTINE"),
        CountryCode_971("UNITED ARAB EMIRATES"),
        CountryCode_972("ISRAEL"),
        CountryCode_973("BAHRAIN"),
        CountryCode_974("QATAR"),
        CountryCode_975("BHUTAN"),
        CountryCode_976("MONGOLIA"),
        CountryCode_977("NEPAL"),
        CountryCode_98("IRAN"),
        CountryCode_992("TAJIKISTAN"),
        CountryCode_993("TURKMENISTAN"),
        CountryCode_994("AZERBAIJAN"),
        CountryCode_995("GEORGIA"),
        CountryCode_996("KYRGYZSTAN"),
        CountryCode_998("UZBEKISTAN"),
        CountryCode_9999("OTHERS");


        private String countryCode;

        FORM_15CA_COUNTRY_DATA (String countryCode) {
            this.countryCode = countryCode;
        }
    }

    public enum FORM_15CA_BANKCODE15CB {

        bankCode15CB_1("9001"),
        bankCode15CB_2("9002"),
        bankCode15CB_3("9003"),
        bankCode15CB_4("9004"),
        bankCode15CB_5("9005"),
        bankCode15CB_6("9006"),
        bankCode15CB_7("9007"),
        bankCode15CB_8("9008"),
        bankCode15CB_9("9009"),
        bankCode15CB_10("9010"),
        bankCode15CB_11("9011"),
        bankCode15CB_12("9012"),
        bankCode15CB_13("9013"),
        bankCode15CB_14("9014"),
        bankCode15CB_15("9015"),
        bankCode15CB_16("9016"),
        bankCode15CB_17("9017"),
        bankCode15CB_18("9018"),
        bankCode15CB_19("9019"),
        bankCode15CB_20("9020"),
        bankCode15CB_21("9021"),
        bankCode15CB_22("9022"),
        bankCode15CB_23("9023"),
        bankCode15CB_24("9024"),
        bankCode15CB_25("9025"),
        bankCode15CB_26("9026"),
        bankCode15CB_27("9027"),
        bankCode15CB_28("9028"),
        bankCode15CB_29("9029"),
        bankCode15CB_30("9030"),
        bankCode15CB_31("9031"),
        bankCode15CB_32("9032"),
        bankCode15CB_33("9033"),
        bankCode15CB_34("9034"),
        bankCode15CB_35("9035"),
        bankCode15CB_36("9036"),
        bankCode15CB_37("9037"),
        bankCode15CB_38("9038"),
        bankCode15CB_39("9039"),
        bankCode15CB_40("9040"),
        bankCode15CB_41("9041"),
        bankCode15CB_42("9042"),
        bankCode15CB_43("9043"),
        bankCode15CB_44("9044"),
        bankCode15CB_45("9045"),
        bankCode15CB_46("9046"),
        bankCode15CB_47("9047"),
        bankCode15CB_48("9048"),
        bankCode15CB_49("9049"),
        bankCode15CB_50("9050"),
        bankCode15CB_51("9051"),
        bankCode15CB_52("9052"),
        bankCode15CB_53("9053"),
        bankCode15CB_54("9054"),
        bankCode15CB_55("9055"),
        bankCode15CB_56("9056"),
        bankCode15CB_57("9057"),
        bankCode15CB_58("9058"),
        bankCode15CB_59("9059"),
        bankCode15CB_60("9060"),
        bankCode15CB_61("9061"),
        bankCode15CB_62("9062"),
        bankCode15CB_63("9063"),
        bankCode15CB_64("9064"),
        bankCode15CB_65("9065"),
        bankCode15CB_66("9066"),
        bankCode15CB_67("9067"),
        bankCode15CB_68("9068"),
        bankCode15CB_69("9069"),
        bankCode15CB_70("9070"),
        bankCode15CB_71("9071"),
        bankCode15CB_72("9072"),
        bankCode15CB_73("9073"),
        bankCode15CB_74("9074"),
        bankCode15CB_75("9075"),
        bankCode15CB_76("9076"),
        bankCode15CB_77("9077"),
        bankCode15CB_78("9078"),
        bankCode15CB_79("9079"),
        bankCode15CB_80("9080"),
        bankCode15CB_81("9081"),
        bankCode15CB_82("9082"),
        bankCode15CB_83("9083"),
        bankCode15CB_84("9084"),
        bankCode15CB_85("9085"),
        bankCode15CB_86("9086"),
        bankCode15CB_87("9087"),
        bankCode15CB_88("9088"),
        bankCode15CB_89("9089"),
        bankCode15CB_90("9090"),
        bankCode15CB_91("9091"),
        bankCode15CB_92("9092"),
        bankCode15CB_93("9093"),
        bankCode15CB_94("9094"),
        bankCode15CB_95("9095"),
        bankCode15CB_96("9096"),
        bankCode15CB_97("9097"),
        bankCode15CB_98("9098"),
        bankCode15CB_99("9099"),
        bankCode15CB_100("9100"),
        bankCode15CB_101("9101"),
        bankCode15CB_102("9102"),
        bankCode15CB_103("9103"),
        bankCode15CB_104("9104"),
        bankCode15CB_105("9105"),
        bankCode15CB_106("9106"),
        bankCode15CB_107("9107"),
        bankCode15CB_108("9108"),
        bankCode15CB_109("9109"),
        bankCode15CB_110("9110"),
        bankCode15CB_111("9111"),
        bankCode15CB_112("9112"),
        bankCode15CB_113("9113"),
        bankCode15CB_114("9114"),
        bankCode15CB_115("9115"),
        bankCode15CB_116("9116"),
        bankCode15CB_117("9117"),
        bankCode15CB_118("9118"),
        bankCode15CB_119("9119"),
        bankCode15CB_120("9120"),
        bankCode15CB_121("9121"),
        bankCode15CB_122("9122"),
        bankCode15CB_123("9123"),
        bankCode15CB_124("9124"),
        bankCode15CB_125("9125"),
        bankCode15CB_126("9126"),
        bankCode15CB_127("9127"),
        bankCode15CB_128("9128"),
        bankCode15CB_129("9129"),
        bankCode15CB_130("9130"),
        bankCode15CB_131("9131"),
        bankCode15CB_132("9132"),
        bankCode15CB_133("9133"),
        bankCode15CB_134("9134"),
        bankCode15CB_135("9135"),
        bankCode15CB_136("9136"),
        bankCode15CB_137("9137"),
        bankCode15CB_138("9138"),
        bankCode15CB_139("9139"),
        bankCode15CB_140("9140"),
        bankCode15CB_141("9141"),
        bankCode15CB_142("9142"),
        bankCode15CB_143("9143"),
        bankCode15CB_144("9144"),
        bankCode15CB_145("9145"),
        bankCode15CB_146("9146"),
        bankCode15CB_147("9147"),
        bankCode15CB_148("9148"),
        bankCode15CB_149("9149"),
        bankCode15CB_150("9150"),
        bankCode15CB_151("9151"),
        bankCode15CB_152("9152"),
        bankCode15CB_153("9153"),
        bankCode15CB_154("9154"),
        bankCode15CB_155("9155"),
        bankCode15CB_156("9156"),
        bankCode15CB_157("9157"),
        bankCode15CB_158("9158"),
        bankCode15CB_159("9159"),
        bankCode15CB_160("9160"),
        bankCode15CB_161("9161"),
        bankCode15CB_162("9162"),
        bankCode15CB_163("9163"),
        bankCode15CB_164("9164"),
        bankCode15CB_165("9165"),
        bankCode15CB_166("9166"),
        bankCode15CB_167("9167"),
        bankCode15CB_168("9168"),
        bankCode15CB_169("9169"),
        bankCode15CB_170("9170"),
        bankCode15CB_171("9171"),
        bankCode15CB_172("9172"),
        bankCode15CB_173("9173"),
        bankCode15CB_174("9174"),
        bankCode15CB_999("999");

        private String bankcode15cb;

        FORM_15CA_BANKCODE15CB(String bankcode15cb) {
            this.bankcode15cb = bankcode15cb;
        }
    }

    public enum DEDUCTOR_STATUS {

        DeductorCode_1("Company"),
        DeductorCode_2("Firm"),
        DeductorCode_3("Individual"),
        DeductorCode_4("Others");
        private String deductorCode;

        DEDUCTOR_STATUS (String deductorCode) {
            this.deductorCode = deductorCode;
        }
    }


    public enum DOMESTIC_FLAG {

        DeductorCode_1("Domestic Company"),
        DeductorCode_2("Foreign Company"),
        DeductorCode_3("Resident"),
        DeductorCode_4("Non-Resident");
        private String deductorCode;

        DOMESTIC_FLAG (String deductorCode) {
            this.deductorCode = deductorCode;
        }
    }


    public enum CERT_SECTION {

        SectCode_1("195(2)"),
        SectCode_2("195(3)"),
        SectCode_3("197"),
        SectCode_4("SELECT");

        private String sectionCode;

        CERT_SECTION (String sectionCode) {
            this.sectionCode = sectionCode;
        }
    }

    public String getCertSection15CBValue(String sectionCode) {
        if (sectionCode != null && !sectionCode.isEmpty()) {
            for (CERT_SECTION s : CERT_SECTION.values()) {
                String enumBankCertSection = s.sectionCode.replaceAll("\\s", "");
                String bankcodeCertSectionAfterAlteration = sectionCode.replaceAll("\\s", "");
                if (enumBankCertSection.equalsIgnoreCase(bankcodeCertSectionAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_4";
    }

    public String getBankCode15CBValue(String bankcode15cb) {
        if (bankcode15cb != null && !bankcode15cb.isEmpty()) {
            for (FORM_15CA_BANKCODE15CB s : FORM_15CA_BANKCODE15CB.values()) {
                String enumBankcode15cb = s.bankcode15cb.replaceAll("\\s", "");
                String bankcode15cbAfterAlteration = bankcode15cb.replaceAll("\\s", "");
                if (enumBankcode15cb.equalsIgnoreCase(bankcode15cbAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_999";
    }

    public String getCurrencyCodeValue(String currency){
        if (currency != null && !currency.isEmpty()) {
            for (FORM_15CA_CURRENCY_DATA s : FORM_15CA_CURRENCY_DATA.values()) {
                String enumCurrency = s.currency.replaceAll("\\s", "");
                String currencyAfterAlteration = currency.replaceAll("\\s", "");
                if (enumCurrency.equalsIgnoreCase(currencyAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_99";
    }

    public String getStateCodeValue(String stateCode){
        if (stateCode != null && !stateCode.isEmpty()) {
            for (FORM_15CA_STATE_DATA s : FORM_15CA_STATE_DATA.values()) {
                String enumStateCode = s.stateCode.replaceAll("\\s", "");
                String stateCodeAfterAlteration = stateCode.replaceAll("\\s", "");
                if (enumStateCode.equalsIgnoreCase(stateCodeAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_0";
    }

    public String getCountryCodeValue(String countryCode){
        if (countryCode != null && !countryCode.isEmpty()) {
            for (FORM_15CA_COUNTRY_DATA s : FORM_15CA_COUNTRY_DATA.values()) {
                String enumCountryCode = s.countryCode.replaceAll("\\s", "").toLowerCase();
                String countryCodeAfterAlteration = countryCode.replaceAll("\\s", "").toLowerCase();
                if (enumCountryCode.equalsIgnoreCase(countryCodeAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "CountryCode_9999";
    }


    public String getCountryRemCodeValue(String countryCode){
        if (countryCode != null && !countryCode.isEmpty()) {
            for (FORM_15CA_COUNTRY_DATA s : FORM_15CA_COUNTRY_DATA.values()) {
                String enumCountryCode = s.countryCode.replaceAll("\\s", "");
                String countryCodeAfterAlteration = countryCode.replaceAll("\\s", "");
                if (enumCountryCode.equalsIgnoreCase(countryCodeAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_9999";
    }

    public String getRemitterStatus(String remitterStatus){
        if (remitterStatus != null && !remitterStatus.isEmpty()) {
            for (DEDUCTOR_STATUS s : DEDUCTOR_STATUS.values()) {
                String enumRemitterStatus = s.deductorCode.replaceAll("\\s", "");
                String remitterStatusAfterAlteration = remitterStatus.replaceAll("\\s", "");
                if (enumRemitterStatus.equalsIgnoreCase(remitterStatusAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_0";
    }


    public String getDomesticFlag(String domesticFlag){
        if (domesticFlag != null && !domesticFlag.isEmpty()) {
            for (DOMESTIC_FLAG s : DOMESTIC_FLAG.values()) {
                String enumDomesticFlag = s.deductorCode.replaceAll("\\s", "");
                String domesticFlagAfterAlteration = domesticFlag.replaceAll("\\s", "");
                if (enumDomesticFlag.equalsIgnoreCase(domesticFlagAfterAlteration)) {
                    return s.name();
                }
            }
        }
        return "0_0";
    }
}