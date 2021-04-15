package rs.io.bzip2;

public final class BZip2InputStream {

	public static int read(byte[] dst, int dstLen, byte[] src, int srcLen, int srcOff) {
		BZip2Context c = new BZip2Context();
		c.src = src;
		c.srcOff = srcOff;
		c.dst = dst;
		c.anInt454 = 0;
		c.srcLen = srcLen;
		c.dstLen = dstLen;
		c.anInt462 = 0;
		c.anInt461 = 0;
		c.anInt451 = 0;
		c.anInt452 = 0;
		c.anInt456 = 0;
		c.anInt457 = 0;
		c.anInt464 = 0;
		method244(c);
		dstLen -= c.dstLen;
		return dstLen;
	}

	private static void method243(BZip2Context c) {
		byte i = c.aByte458;
		int i_3_ = c.anInt459;
		int i_4_ = c.anInt469;
		int i_5_ = c.anInt467;
		int[] is = BZip2Context.anIntArray472;
		int i_6_ = c.anInt466;
		byte[] is_7_ = c.dst;
		int i_8_ = c.anInt454;
		int dstLen = c.dstLen;
		int i_10_ = dstLen;
		int i_11_ = c.anInt486 + 1;
		while_4_:
		for (;;) {
			if (i_3_ > 0) {
				for (;;) {
					if (dstLen == 0) {
						break while_4_;
					}
					if (i_3_ == 1) {
						break;
					}
					is_7_[i_8_] = i;
					i_3_--;
					i_8_++;
					dstLen--;
				}
				if (dstLen == 0) {
					i_3_ = 1;
					break;
				}
				is_7_[i_8_] = i;
				i_8_++;
				dstLen--;
			}
			boolean bool = true;
			while (bool) {
				bool = false;
				if (i_4_ == i_11_) {
					i_3_ = 0;
					break while_4_;
				}
				i = (byte) i_5_;
				i_6_ = is[i_6_];
				int i_12_ = (byte) (i_6_ & 0xff);
				i_6_ >>= 8;
				i_4_++;
				if (i_12_ != i_5_) {
					i_5_ = i_12_;
					if (dstLen == 0) {
						i_3_ = 1;
						break while_4_;
					}
					is_7_[i_8_] = i;
					i_8_++;
					dstLen--;
					bool = true;
				} else if (i_4_ == i_11_) {
					if (dstLen == 0) {
						i_3_ = 1;
						break while_4_;
					}
					is_7_[i_8_] = i;
					i_8_++;
					dstLen--;
					bool = true;
				}
			}
			i_3_ = 2;
			i_6_ = is[i_6_];
			int i_13_ = (byte) (i_6_ & 0xff);
			i_6_ >>= 8;
			if (++i_4_ != i_11_) {
				if (i_13_ != i_5_) {
					i_5_ = i_13_;
				} else {
					i_3_ = 3;
					i_6_ = is[i_6_];
					i_13_ = (byte) (i_6_ & 0xff);
					i_6_ >>= 8;
					if (++i_4_ != i_11_) {
						if (i_13_ != i_5_) {
							i_5_ = i_13_;
						} else {
							i_6_ = is[i_6_];
							i_13_ = (byte) (i_6_ & 0xff);
							i_6_ >>= 8;
							i_4_++;
							i_3_ = (i_13_ & 0xff) + 4;
							i_6_ = is[i_6_];
							i_5_ = (byte) (i_6_ & 0xff);
							i_6_ >>= 8;
							i_4_++;
						}
					}
				}
			}
		}
		int i_14_ = c.anInt456;
		c.anInt456 += i_10_ - dstLen;
		if (c.anInt456 < i_14_) {
			c.anInt457++;
		}
		c.aByte458 = i;
		c.anInt459 = i_3_;
		c.anInt469 = i_4_;
		c.anInt467 = i_5_;
		BZip2Context.anIntArray472 = is;
		c.anInt466 = i_6_;
		c.dst = is_7_;
		c.anInt454 = i_8_;
		c.dstLen = dstLen;
	}

	private static void method244(BZip2Context c) {
		int i = 0;
		int[] is = null;
		int[] is_33_ = null;
		int[] is_34_ = null;
		c.anInt463 = 1;
		if (BZip2Context.anIntArray472 == null) {
			BZip2Context.anIntArray472 = new int[c.anInt463 * 100000];
		}
		boolean bool_35_ = true;
		while (bool_35_) {
			byte i_36_ = method245(c);
			if (i_36_ == 23) {
				break;
			}
			i_36_ = method245(c);
			i_36_ = method245(c);
			i_36_ = method245(c);
			i_36_ = method245(c);
			i_36_ = method245(c);
			c.anInt464++;
			i_36_ = method245(c);
			i_36_ = method245(c);
			i_36_ = method245(c);
			i_36_ = method245(c);
			i_36_ = method246(c);
			if (i_36_ != 0) {
				c.aBoolean460 = true;
			} else {
				c.aBoolean460 = false;
			}
			if (c.aBoolean460) {
				System.out.println("PANIC! RANDOMISED BLOCK!");
			}
			c.anInt465 = 0;
			int i_37_ = method245(c);
			c.anInt465 = c.anInt465 << 8 | i_37_ & 0xff;
			i_37_ = method245(c);
			c.anInt465 = c.anInt465 << 8 | i_37_ & 0xff;
			i_37_ = method245(c);
			c.anInt465 = c.anInt465 << 8 | i_37_ & 0xff;
			for (int i_38_ = 0; i_38_ < 16; i_38_++) {
				i_36_ = method246(c);
				if (i_36_ == 1) {
					c.aBooleanArray475[i_38_] = true;
				} else {
					c.aBooleanArray475[i_38_] = false;
				}
			}
			for (int i_39_ = 0; i_39_ < 256; i_39_++) {
				c.aBooleanArray474[i_39_] = false;
			}
			for (int i_40_ = 0; i_40_ < 16; i_40_++) {
				if (c.aBooleanArray475[i_40_]) {
					for (int i_41_ = 0; i_41_ < 16; i_41_++) {
						i_36_ = method246(c);
						if (i_36_ == 1) {
							c.aBooleanArray474[i_40_ * 16 + i_41_] = true;
						}
					}
				}
			}
			method248(c);
			int i_42_ = c.anInt473 + 2;
			int i_43_ = method247(3, c);
			int i_44_ = method247(15, c);
			for (int i_45_ = 0; i_45_ < i_44_; i_45_++) {
				int i_46_ = 0;
				for (;;) {
					i_36_ = method246(c);
					if (i_36_ == 0) {
						break;
					}
					i_46_++;
				}
				c.aByteArray480[i_45_] = (byte) i_46_;
			}
			byte[] is_47_ = new byte[6];
			for (byte i_48_ = 0; i_48_ < i_43_; i_48_++) {
				is_47_[i_48_] = i_48_;
			}
			for (int i_49_ = 0; i_49_ < i_44_; i_49_++) {
				byte i_50_ = c.aByteArray480[i_49_];
				byte i_51_ = is_47_[i_50_];
				for (/**/; i_50_ > 0; i_50_--) {
					is_47_[i_50_] = is_47_[i_50_ - 1];
				}
				is_47_[0] = i_51_;
				c.aByteArray479[i_49_] = i_51_;
			}
			for (int i_52_ = 0; i_52_ < i_43_; i_52_++) {
				int i_53_ = method247(5, c);
				for (int i_54_ = 0; i_54_ < i_42_; i_54_++) {
					for (;;) {
						i_36_ = method246(c);
						if (i_36_ == 0) {
							break;
						}
						i_36_ = method246(c);
						if (i_36_ == 0) {
							i_53_++;
						} else {
							i_53_--;
						}
					}
					c.aByteArrayArray481[i_52_][i_54_] = (byte) i_53_;
				}
			}
			for (int i_55_ = 0; i_55_ < i_43_; i_55_++) {
				int i_56_ = 32;
				byte i_57_ = 0;
				for (int i_58_ = 0; i_58_ < i_42_; i_58_++) {
					if (c.aByteArrayArray481[i_55_][i_58_] > i_57_) {
						i_57_ = c.aByteArrayArray481[i_55_][i_58_];
					}
					if (c.aByteArrayArray481[i_55_][i_58_] < i_56_) {
						i_56_ = c.aByteArrayArray481[i_55_][i_58_];
					}
				}
				method249(c.anIntArrayArray482[i_55_], c.anIntArrayArray483[i_55_], c.anIntArrayArray484[i_55_], c.aByteArrayArray481[i_55_], i_56_, i_57_, i_42_);
				c.anIntArray485[i_55_] = i_56_;
			}
			int i_59_ = c.anInt473 + 1;
			int i_61_ = -1;
			int i_62_ = 0;
			for (int i_63_ = 0; i_63_ <= 255; i_63_++) {
				c.anIntArray468[i_63_] = 0;
			}
			int i_64_ = 4095;
			for (int i_65_ = 15; i_65_ >= 0; i_65_--) {
				for (int i_66_ = 15; i_66_ >= 0; i_66_--) {
					c.aByteArray477[i_64_] = (byte) (i_65_ * 16 + i_66_);
					i_64_--;
				}
				c.anIntArray478[i_65_] = i_64_ + 1;
			}
			int i_67_ = 0;
			if (i_62_ == 0) {
				i_61_++;
				i_62_ = 50;
				byte i_68_ = c.aByteArray479[i_61_];
				i = c.anIntArray485[i_68_];
				is = c.anIntArrayArray482[i_68_];
				is_34_ = c.anIntArrayArray484[i_68_];
				is_33_ = c.anIntArrayArray483[i_68_];
			}
			i_62_--;
			int i_69_ = i;
			int i_70_;
			int i_71_;
			for (i_71_ = method247(i_69_, c); i_71_ > is[i_69_]; i_71_ = i_71_ << 1 | i_70_) {
				i_69_++;
				i_70_ = method246(c);
			}
			int i_72_ = is_34_[i_71_ - is_33_[i_69_]];
			while (i_72_ != i_59_) {
				if (i_72_ == 0 || i_72_ == 1) {
					int i_73_ = -1;
					int i_74_ = 1;
					do {
						if (i_72_ == 0) {
							i_73_ += i_74_;
						} else if (i_72_ == 1) {
							i_73_ += i_74_ * 2;
						}
						i_74_ *= 2;
						if (i_62_ == 0) {
							i_61_++;
							i_62_ = 50;
							byte i_75_ = c.aByteArray479[i_61_];
							i = c.anIntArray485[i_75_];
							is = c.anIntArrayArray482[i_75_];
							is_34_ = c.anIntArrayArray484[i_75_];
							is_33_ = c.anIntArrayArray483[i_75_];
						}
						i_62_--;
						i_69_ = i;
						for (i_71_ = method247(i_69_, c); i_71_ > is[i_69_]; i_71_ = i_71_ << 1 | i_70_) {
							i_69_++;
							i_70_ = method246(c);
						}
						i_72_ = is_34_[i_71_ - is_33_[i_69_]];
					} while (i_72_ == 0 || i_72_ == 1);
					i_73_++;
					i_37_ = (c.aByteArray476[(c.aByteArray477[c.anIntArray478[0]] & 0xff)]);
					c.anIntArray468[i_37_ & 0xff] += i_73_;
					for (/**/; i_73_ > 0; i_73_--) {
						BZip2Context.anIntArray472[i_67_] = i_37_ & 0xff;
						i_67_++;
					}
				} else {
					int i_76_ = i_72_ - 1;
					if (i_76_ < 16) {
						int i_77_ = c.anIntArray478[0];
						i_36_ = c.aByteArray477[i_77_ + i_76_];
						for (/**/; i_76_ > 3; i_76_ -= 4) {
							int i_78_ = i_77_ + i_76_;
							c.aByteArray477[i_78_] = c.aByteArray477[i_78_ - 1];
							c.aByteArray477[i_78_ - 1] = c.aByteArray477[i_78_ - 2];
							c.aByteArray477[i_78_ - 2] = c.aByteArray477[i_78_ - 3];
							c.aByteArray477[i_78_ - 3] = c.aByteArray477[i_78_ - 4];
						}
						for (/**/; i_76_ > 0; i_76_--) {
							c.aByteArray477[i_77_ + i_76_] = c.aByteArray477[i_77_ + i_76_ - 1];
						}
						c.aByteArray477[i_77_] = i_36_;
					} else {
						int i_79_ = i_76_ / 16;
						int i_80_ = i_76_ % 16;
						int i_81_ = c.anIntArray478[i_79_] + i_80_;
						i_36_ = c.aByteArray477[i_81_];
						for (/**/; i_81_ > c.anIntArray478[i_79_]; i_81_--) {
							c.aByteArray477[i_81_] = c.aByteArray477[i_81_ - 1];
						}
						c.anIntArray478[i_79_]++;
						for (/**/; i_79_ > 0; i_79_--) {
							c.anIntArray478[i_79_]--;
							c.aByteArray477[c.anIntArray478[i_79_]] = (c.aByteArray477[(c.anIntArray478[i_79_ - 1] + 16 - 1)]);
						}
						c.anIntArray478[0]--;
						c.aByteArray477[c.anIntArray478[0]] = i_36_;
						if (c.anIntArray478[0] == 0) {
							int i_82_ = 4095;
							for (int i_83_ = 15; i_83_ >= 0; i_83_--) {
								for (int i_84_ = 15; i_84_ >= 0; i_84_--) {
									c.aByteArray477[i_82_] = (c.aByteArray477[(c.anIntArray478[i_83_] + i_84_)]);
									i_82_--;
								}
								c.anIntArray478[i_83_] = i_82_ + 1;
							}
						}
					}
					c.anIntArray468[(c.aByteArray476[i_36_ & 0xff] & 0xff)]++;
					BZip2Context.anIntArray472[i_67_] = c.aByteArray476[i_36_ & 0xff] & 0xff;
					i_67_++;
					if (i_62_ == 0) {
						i_61_++;
						i_62_ = 50;
						byte i_85_ = c.aByteArray479[i_61_];
						i = c.anIntArray485[i_85_];
						is = c.anIntArrayArray482[i_85_];
						is_34_ = c.anIntArrayArray484[i_85_];
						is_33_ = c.anIntArrayArray483[i_85_];
					}
					i_62_--;
					i_69_ = i;
					for (i_71_ = method247(i_69_, c); i_71_ > is[i_69_]; i_71_ = i_71_ << 1 | i_70_) {
						i_69_++;
						i_70_ = method246(c);
					}
					i_72_ = is_34_[i_71_ - is_33_[i_69_]];
				}
			}
			c.anInt459 = 0;
			c.aByte458 = (byte) 0;
			c.anIntArray470[0] = 0;
			for (int i_86_ = 1; i_86_ <= 256; i_86_++) {
				c.anIntArray470[i_86_] = c.anIntArray468[i_86_ - 1];
			}
			for (int i_87_ = 1; i_87_ <= 256; i_87_++) {
				c.anIntArray470[i_87_] += c.anIntArray470[i_87_ - 1];
			}
			for (int i_88_ = 0; i_88_ < i_67_; i_88_++) {
				i_37_ = (byte) (BZip2Context.anIntArray472[i_88_] & 0xff);
				BZip2Context.anIntArray472[c.anIntArray470[i_37_ & 0xff]] |= i_88_ << 8;
				c.anIntArray470[i_37_ & 0xff]++;
			}
			c.anInt466 = BZip2Context.anIntArray472[c.anInt465] >> 8;
			c.anInt469 = 0;
			c.anInt466 = BZip2Context.anIntArray472[c.anInt466];
			c.anInt467 = (byte) (c.anInt466 & 0xff);
			c.anInt466 >>= 8;
			c.anInt469++;
			c.anInt486 = i_67_;
			method243(c);
			if (c.anInt469 == c.anInt486 + 1 && c.anInt459 == 0) {
				bool_35_ = true;
			} else {
				bool_35_ = false;
			}
		}
	}

	private static byte method245(BZip2Context class30) {
		return (byte) method247(8, class30);
	}

	private static byte method246(BZip2Context class30) {
		return (byte) method247(1, class30);
	}

	private static int method247(int i, BZip2Context class30) {
		int i_89_;
		for (;;) {
			if (class30.anInt462 >= i) {
				int i_90_ = class30.anInt461 >> class30.anInt462 - i & (1 << i) - 1;
				class30.anInt462 -= i;
				i_89_ = i_90_;
				break;
			}
			class30.anInt461 = (class30.anInt461 << 8 | class30.src[class30.srcOff] & 0xff);
			class30.anInt462 += 8;
			class30.srcOff++;
			class30.srcLen--;
			class30.anInt451++;
			if (class30.anInt451 == 0) {
				class30.anInt452++;
			}
		}
		return i_89_;
	}

	private static void method248(BZip2Context class30) {
		class30.anInt473 = 0;
		for (int i = 0; i < 256; i++) {
			if (class30.aBooleanArray474[i]) {
				class30.aByteArray476[class30.anInt473] = (byte) i;
				class30.anInt473++;
			}
		}
	}

	private static void method249(int[] is, int[] is_91_, int[] is_92_, byte[] is_93_, int i, int i_94_, int i_95_) {
		int i_96_ = 0;
		for (int i_97_ = i; i_97_ <= i_94_; i_97_++) {
			for (int i_98_ = 0; i_98_ < i_95_; i_98_++) {
				if (is_93_[i_98_] == i_97_) {
					is_92_[i_96_] = i_98_;
					i_96_++;
				}
			}
		}
		for (int i_99_ = 0; i_99_ < 23; i_99_++) {
			is_91_[i_99_] = 0;
		}
		for (int i_100_ = 0; i_100_ < i_95_; i_100_++) {
			is_91_[is_93_[i_100_] + 1]++;
		}
		for (int i_101_ = 1; i_101_ < 23; i_101_++) {
			is_91_[i_101_] += is_91_[i_101_ - 1];
		}
		for (int i_102_ = 0; i_102_ < 23; i_102_++) {
			is[i_102_] = 0;
		}
		int i_103_ = 0;
		for (int i_104_ = i; i_104_ <= i_94_; i_104_++) {
			i_103_ += is_91_[i_104_ + 1] - is_91_[i_104_];
			is[i_104_] = i_103_ - 1;
			i_103_ <<= 1;
		}
		for (int i_105_ = i + 1; i_105_ <= i_94_; i_105_++) {
			is_91_[i_105_] = (is[i_105_ - 1] + 1 << 1) - is_91_[i_105_];
		}
	}
}
