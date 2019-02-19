package euphoria.psycho.knife.server;

class DataProvider {





    private static final byte[][] mIndex = new byte[][]{
/* 0 67 title */ new byte[]{60,33,68,79,67,84,89,80,69,32,104,116,109,108,62,60,104,116,109,108,32,108,97,110,103,61,34,101,110,34,62,60,104,101,97,100,62,60,109,101,116,97,32,99,104,97,114,115,101,116,61,34,85,84,70,45,56,34,47,62,60,116,105,116,108,101,62},
/* 1 3784 list */ new byte[]{60,47,116,105,116,108,101,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,114,101,115,101,116,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,97,112,112,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,108,97,121,111,117,116,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,97,99,116,105,111,110,95,112,97,110,101,108,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,105,99,111,110,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,108,105,115,116,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,109,101,110,117,46,99,115,115,34,47,62,60,108,105,110,107,32,114,101,108,61,34,115,116,121,108,101,115,104,101,101,116,34,32,104,114,101,102,61,34,116,111,111,108,98,97,114,46,99,115,115,34,47,62,60,98,111,100,121,32,99,108,97,115,115,61,34,112,97,103,101,45,104,111,109,101,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,95,119,114,97,112,112,101,114,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,45,116,111,111,108,98,97,114,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,45,116,111,111,108,98,97,114,45,105,110,110,101,114,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,110,97,118,32,99,108,101,97,114,102,105,120,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,32,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,45,97,32,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,45,117,112,108,111,97,100,32,99,108,101,97,114,102,105,120,34,62,60,100,105,118,32,105,100,61,34,102,111,114,109,70,105,108,101,73,110,112,117,116,67,116,34,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,45,99,111,110,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,117,112,108,111,97,100,45,115,34,62,60,47,105,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,97,99,116,45,116,120,116,34,62,(byte)228,(byte)184,(byte)138,(byte)228,(byte)188,(byte)160,60,47,115,112,97,110,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,98,117,98,98,108,101,45,109,101,110,117,32,119,105,116,104,45,98,111,114,100,101,114,34,62,60,117,108,32,99,108,97,115,115,61,34,109,101,110,117,45,108,105,115,116,34,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,32,105,100,61,34,117,112,108,111,97,100,70,105,108,101,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,100,111,99,45,115,34,62,60,47,105,62,(byte)230,(byte)150,(byte)135,(byte)228,(byte)187,(byte)182,60,47,115,112,97,110,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,102,105,108,101,45,115,34,62,60,47,105,62,(byte)230,(byte)150,(byte)135,(byte)228,(byte)187,(byte)182,(byte)229,(byte)164,(byte)185,60,47,115,112,97,110,62,60,47,117,108,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,32,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,45,99,114,101,97,116,101,32,99,108,101,97,114,102,105,120,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,45,99,111,110,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,97,100,100,34,62,60,47,105,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,97,99,116,45,116,120,116,34,62,(byte)230,(byte)150,(byte)176,(byte)229,(byte)187,(byte)186,60,47,115,112,97,110,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,98,117,98,98,108,101,45,109,101,110,117,32,119,105,116,104,45,98,111,114,100,101,114,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,98,100,34,62,60,117,108,32,99,108,97,115,115,61,34,109,101,110,117,45,108,105,115,116,34,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,99,114,101,97,116,101,45,115,34,62,60,47,105,62,(byte)230,(byte)150,(byte)176,(byte)229,(byte)187,(byte)186,(byte)230,(byte)150,(byte)135,(byte)228,(byte)187,(byte)182,(byte)229,(byte)164,(byte)185,60,47,115,112,97,110,62,60,100,105,118,32,99,108,97,115,115,61,34,115,112,108,105,116,101,114,34,62,60,47,100,105,118,62,60,47,117,108,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,104,100,34,62,60,112,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,104,100,45,116,105,116,34,62,(byte)232,(byte)133,(byte)190,(byte)232,(byte)174,(byte)175,(byte)230,(byte)150,(byte)135,(byte)230,(byte)161,(byte)163,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,98,100,34,62,60,117,108,32,99,108,97,115,115,61,34,109,101,110,117,45,108,105,115,116,34,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,116,100,111,99,45,115,34,62,60,47,105,62,(byte)229,(byte)156,(byte)168,(byte)231,(byte)186,(byte)191,(byte)230,(byte)150,(byte)135,(byte)230,(byte)161,(byte)163,60,47,115,112,97,110,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,116,120,108,115,45,115,34,62,60,47,105,62,(byte)229,(byte)156,(byte)168,(byte)231,(byte)186,(byte)191,(byte)232,(byte)161,(byte)168,(byte)230,(byte)160,(byte)188,60,47,115,112,97,110,62,60,100,105,118,32,99,108,97,115,115,61,34,115,112,108,105,116,101,114,34,62,60,47,100,105,118,62,60,47,117,108,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,104,100,34,62,60,112,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,104,100,45,116,105,116,34,62,79,102,102,105,99,101,(byte)230,(byte)150,(byte)135,(byte)230,(byte)161,(byte)163,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,98,100,34,62,60,117,108,32,99,108,97,115,115,61,34,109,101,110,117,45,108,105,115,116,34,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,119,111,114,100,45,115,34,62,60,47,105,62,87,111,114,100,32,(byte)230,(byte)150,(byte)135,(byte)230,(byte)161,(byte)163,60,47,115,112,97,110,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,120,108,115,45,115,34,62,60,47,105,62,69,120,99,101,108,32,(byte)232,(byte)161,(byte)168,(byte)230,(byte)160,(byte)188,60,47,115,112,97,110,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,112,112,116,45,115,34,62,60,47,105,62,80,80,84,32,(byte)229,(byte)185,(byte)187,(byte)231,(byte)129,(byte)175,(byte)231,(byte)137,(byte)135,60,47,115,112,97,110,62,60,100,105,118,32,99,108,97,115,115,61,34,115,112,108,105,116,101,114,34,62,60,47,100,105,118,62,60,47,117,108,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,45,98,100,34,62,60,117,108,32,99,108,97,115,115,61,34,109,101,110,117,45,108,105,115,116,34,62,60,108,105,32,99,108,97,115,115,61,34,109,101,110,117,45,105,116,101,109,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,98,116,45,115,34,62,60,47,105,62,(byte)231,(byte)166,(byte)187,(byte)231,(byte)186,(byte)191,(byte)228,(byte)184,(byte)139,(byte)232,(byte)189,(byte)189,60,47,115,112,97,110,62,60,47,117,108,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,45,109,97,105,110,45,119,114,97,112,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,45,109,97,105,110,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,45,109,97,105,110,45,104,100,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,97,99,116,45,112,97,110,101,108,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,45,112,97,110,101,108,45,105,110,110,101,114,32,99,108,101,97,114,102,105,120,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,32,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,45,99,32,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,45,100,32,109,111,100,45,97,99,116,105,111,110,45,119,114,97,112,45,109,111,100,101,32,99,108,101,97,114,102,105,120,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,32,97,99,116,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,45,99,111,110,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,109,111,100,101,45,108,105,115,116,34,62,60,47,105,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,97,99,116,45,116,120,116,34,62,(byte)229,(byte)136,(byte)151,(byte)232,(byte)161,(byte)168,60,47,115,112,97,110,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,34,62,60,100,105,118,32,99,108,97,115,115,61,34,97,99,116,105,111,110,45,105,116,101,109,45,99,111,110,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,109,111,100,101,45,116,104,117,109,98,34,62,60,47,105,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,97,99,116,45,116,120,116,34,62,(byte)231,(byte)188,(byte)169,(byte)231,(byte)149,(byte)165,(byte)229,(byte)155,(byte)190,60,47,115,112,97,110,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,98,114,101,97,100,99,114,117,109,98,34,62,60,117,108,32,99,108,97,115,115,61,34,98,114,101,97,100,99,114,117,109,98,32,99,108,101,97,114,102,105,120,34,62,60,108,105,32,99,108,97,115,115,61,34,105,116,101,109,32,97,108,108,32,99,117,114,34,62,60,97,32,104,114,101,102,61,34,106,97,118,97,115,99,114,105,112,116,58,118,111,105,100,40,48,41,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,34,62,60,47,105,62,(byte)229,(byte)133,(byte)168,(byte)233,(byte)131,(byte)168,60,47,97,62,60,47,117,108,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,121,111,117,116,45,109,97,105,110,45,98,100,34,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,108,105,115,116,45,103,114,111,117,112,32,109,111,100,45,108,105,115,116,45,103,114,111,117,112,45,102,105,108,101,115,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,104,100,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,116,105,116,45,119,114,97,112,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,116,105,116,32,110,97,109,101,32,117,112,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,105,116,45,99,111,110,34,62,(byte)229,(byte)144,(byte)141,(byte)231,(byte)167,(byte)176,60,105,32,99,108,97,115,115,61,34,105,99,111,110,34,62,60,47,105,62,60,47,115,112,97,110,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,116,105,116,32,116,105,109,101,32,99,117,114,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,105,116,45,99,111,110,34,62,(byte)228,(byte)184,(byte)138,(byte)230,(byte)172,(byte)161,(byte)228,(byte)191,(byte)174,(byte)230,(byte)148,(byte)185,(byte)230,(byte)151,(byte)182,(byte)233,(byte)151,(byte)180,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,115,111,114,116,34,62,60,47,105,62,60,47,115,112,97,110,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,116,105,116,32,115,105,122,101,32,100,105,115,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,116,105,116,45,99,111,110,34,62,(byte)229,(byte)164,(byte)167,(byte)229,(byte)176,(byte)143,60,105,32,99,108,97,115,115,61,34,105,99,111,110,34,62,60,47,105,62,60,47,115,112,97,110,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,116,97,98,105,110,100,101,120,61,34,48,34,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,98,100,34,32,115,101,108,101,99,116,98,111,120,45,105,100,61,34,49,34,32,100,114,97,103,100,114,111,112,45,105,100,61,34,49,34,32,115,99,114,111,108,108,45,98,111,116,116,111,109,45,105,100,61,34,49,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,119,114,97,112,112,101,114,34,62,60,117,108,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,34,62},
/* 2 257 count */ new byte[]{60,47,117,108,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,109,111,100,45,108,111,97,100,109,111,114,101,34,32,115,116,121,108,101,61,34,100,105,115,112,108,97,121,58,110,111,110,101,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,108,111,97,100,45,98,117,108,108,101,116,45,108,101,102,116,34,62,60,47,105,62,32,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,108,111,97,100,45,98,117,108,108,101,116,34,62,60,47,105,62,32,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,108,111,97,100,45,98,117,108,108,101,116,45,114,105,103,104,116,34,62,60,47,105,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,105,110,102,111,34,62,60,112,32,99,108,97,115,115,61,34,105,110,102,111,45,99,111,110,34,62,(byte)229,(byte)133,(byte)177,60,98,32,99,108,97,115,115,61,34,99,111,117,110,116,34,62},
/* 3 439  */ new byte[]{60,47,98,62,(byte)233,(byte)161,(byte)185,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,100,105,118,62,60,115,99,114,105,112,116,32,115,114,99,61,34,100,111,109,46,106,115,34,62,60,47,115,99,114,105,112,116,62,60,115,99,114,105,112,116,32,115,114,99,61,34,117,112,108,111,97,100,46,106,115,34,62,60,47,115,99,114,105,112,116,62,60,115,99,114,105,112,116,32,115,114,99,61,34,97,112,112,46,106,115,34,62,60,47,115,99,114,105,112,116,62,60,100,105,118,32,105,100,61,34,95,100,110,100,34,32,99,108,97,115,115,61,34,117,105,45,100,110,100,34,32,115,116,121,108,101,61,34,112,111,115,105,116,105,111,110,58,102,105,120,101,100,59,122,45,105,110,100,101,120,58,57,57,57,57,59,98,97,99,107,103,114,111,117,110,100,45,99,111,108,111,114,58,114,103,98,40,50,52,57,44,50,52,53,44,50,52,53,41,59,111,112,97,99,105,116,121,58,48,46,50,59,98,111,114,100,101,114,58,53,112,120,32,100,97,115,104,101,100,59,116,111,112,58,48,59,108,101,102,116,58,48,59,100,105,115,112,108,97,121,58,110,111,110,101,34,62,60,100,105,118,32,115,116,121,108,101,61,34,116,111,112,58,53,48,37,59,108,101,102,116,58,53,48,37,59,109,97,114,103,105,110,45,108,101,102,116,58,45,53,48,112,120,59,109,97,114,103,105,110,45,116,111,112,58,45,53,48,112,120,59,102,111,110,116,45,115,105,122,101,58,50,52,112,120,59,111,112,97,99,105,116,121,58,49,59,112,111,115,105,116,105,111,110,58,97,98,115,111,108,117,116,101,34,62,(byte)230,(byte)150,(byte)135,(byte)228,(byte)187,(byte)182,(byte)230,(byte)139,(byte)150,(byte)230,(byte)148,(byte)190,(byte)229,(byte)136,(byte)176,(byte)230,(byte)173,(byte)164,(byte)229,(byte)164,(byte)132,60,47,100,105,118,62,60,47,100,105,118,62},
    };
// /*title*/os.write(bytes[0]);
// /*list*/os.write(bytes[1]);
// /*count*/os.write(bytes[2]);
// /**/os.write(bytes[3]);



    private static final byte[][] mFileList = new byte[][]{
/* 0 195 icon */ new byte[]{60,108,105,32,99,108,97,115,115,61,34,108,105,115,116,45,103,114,111,117,112,45,105,116,101,109,32,99,104,101,99,107,101,100,34,62,60,100,105,118,32,99,108,97,115,115,61,34,105,116,101,109,45,105,110,110,101,114,34,62,60,100,105,118,32,99,108,97,115,115,61,34,105,116,101,109,45,116,105,116,34,62,60,100,105,118,32,99,108,97,115,115,61,34,108,97,98,101,108,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,99,104,101,99,107,45,115,32,105,99,111,110,45,99,104,101,99,107,98,111,120,34,62,60,47,105,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,116,104,117,109,98,34,62,60,105,32,99,108,97,115,115,61,34,105,99,111,110,32,105,99,111,110,45,109,32},
/* 1 39 href */ new byte[]{34,62,60,47,105,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,105,110,102,111,34,62,60,97,32,104,114,101,102,61,34},
/* 2 29 tit */ new byte[]{34,32,116,105,116,108,101,61,34,(byte)229,(byte)189,(byte)146,(byte)230,(byte)161,(byte)163,34,32,99,108,97,115,115,61,34,116,105,116,34,62},
/* 3 96 txt-time */ new byte[]{60,47,97,62,60,47,100,105,118,62,60,47,100,105,118,62,60,100,105,118,32,99,108,97,115,115,61,34,105,116,101,109,45,105,110,102,111,34,62,60,115,112,97,110,32,99,108,97,115,115,61,34,105,116,101,109,45,105,110,102,111,45,108,105,115,116,34,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,32,116,120,116,45,116,105,109,101,34,62},
/* 4 72 size */ new byte[]{60,47,115,112,97,110,62,60,47,115,112,97,110,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,105,116,101,109,45,105,110,102,111,45,108,105,115,116,34,62,32,60,115,112,97,110,32,99,108,97,115,115,61,34,116,120,116,32,116,120,116,45,115,105,122,101,34,62},
/* 5 32  */ new byte[]{60,47,115,112,97,110,62,32,60,47,115,112,97,110,62,60,47,100,105,118,62,60,47,100,105,118,62,60,47,108,105,62},

    };

    public static byte[][] getFileList() {
        return mFileList;
    }

    public static byte[][] getIndex() {
        return mIndex;
    }
}
