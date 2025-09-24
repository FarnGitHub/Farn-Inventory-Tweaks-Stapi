package net.invtweaks.config;

import java.awt.Point;
import java.util.logging.Logger;

import net.invtweaks.tree.ItemTree;

public class SortingRule implements Comparable {
	private String constraint;
	private int[] preferredPositions;
	private String keyword;
	private RuleType type;
	private int priority;
	private int containerSize;
	private int containerRowSize;

	public SortingRule(ItemTree tree, String constraint, String keyword, int containerSize, int containerRowSize) {
		this.keyword = keyword;
		this.constraint = constraint;
		this.type = getRuleType(constraint);
		this.containerSize = containerSize;
		this.containerRowSize = containerRowSize;
		this.preferredPositions = this.getRulePreferredPositions(constraint);
		this.priority = this.type.getLowestPriority() + 100000 + tree.getKeywordDepth(keyword) * 1000 - tree.getKeywordOrder(keyword);
	}

	public RuleType getType() {
		return this.type;
	}

	public int[] getPreferredSlots() {
		return this.preferredPositions;
	}

	public String getKeyword() {
		return this.keyword;
	}

	public String getRawConstraint() {
		return this.constraint;
	}

	public int getPriority() {
		return this.priority;
	}

	public int compareTo(SortingRule o) {
		return this.getPriority() - o.getPriority();
	}

	public int[] getRulePreferredPositions(String constraint) {
		return getRulePreferredPositions(constraint, this.containerSize, this.containerRowSize);
	}

	public static int[] getRulePreferredPositions(String constraint, int containerSize, int containerRowSize) {
		int[] result = null;
		int containerColumnSize = containerSize / containerRowSize;
		if(constraint.length() >= 5) {
			boolean column = false;
			if(constraint.contains("v")) {
				column = true;
				constraint = constraint.replaceAll("v", "");
			}

			String[] row = constraint.split("-");
			if(row.length == 2) {
				int[] reverse = getRulePreferredPositions(row[0], containerSize, containerRowSize);
				int[] i = getRulePreferredPositions(row[1], containerSize, containerRowSize);
				if(reverse.length == 1 && i.length == 1) {
					int c = reverse[0];
					int slot2 = i[0];
					Point point1 = new Point(c % containerRowSize, c / containerRowSize);
					Point point2 = new Point(slot2 % containerRowSize, slot2 / containerRowSize);
					result = new int[(Math.abs(point2.y - point1.y) + 1) * (Math.abs(point2.x - point1.x) + 1)];
					int resultIndex = 0;
					int x;
					if(column) {
						Point[] y = new Point[]{point1, point2};
						x = y.length;

						for(int i$ = 0; i$ < x; ++i$) {
							Point p = y[i$];
							int buffer = p.x;
							p.x = p.y;
							p.y = buffer;
						}
					}

					int i24 = point1.y;

					while(true) {
						if(point1.y < point2.y) {
							if(i24 > point2.y) {
								break;
							}
						} else if(i24 < point2.y) {
							break;
						}

						x = point1.x;

						while(true) {
							if(point1.x < point2.x) {
								if(x > point2.x) {
									break;
								}
							} else if(x < point2.x) {
								break;
							}

							result[resultIndex++] = column ? index(containerRowSize, x, i24) : index(containerRowSize, i24, x);
							x += point1.x < point2.x ? 1 : -1;
						}

						i24 += point1.y < point2.y ? 1 : -1;
					}

					if(constraint.contains("r")) {
						reverseArray(result);
					}
				}
			}
		} else {
			int i19 = -1;
			int i20 = -1;
			boolean z21 = false;

			int i22;
			for(i22 = 0; i22 < constraint.length(); ++i22) {
				char c23 = constraint.charAt(i22);
				if(c23 <= 57) {
					i19 = c23 - 49;
				} else if(c23 == 114) {
					z21 = true;
				} else {
					i20 = c23 - 97;
				}
			}

			if(i19 != -1 && i20 != -1) {
				result = new int[]{index(containerRowSize, i20, i19)};
			} else if(i20 != -1) {
				result = new int[containerRowSize];

				for(i22 = 0; i22 < containerRowSize; ++i22) {
					result[i22] = index(containerRowSize, i20, z21 ? containerRowSize - 1 - i22 : i22);
				}
			} else {
				result = new int[containerColumnSize];

				for(i22 = 0; i22 < containerColumnSize; ++i22) {
					result[i22] = index(containerRowSize, z21 ? i22 : containerColumnSize - 1 - i22, i19);
				}
			}
		}

		return result;
	}

	public static RuleType getRuleType(String constraint) {
		RuleType result = RuleType.TILE;
		if(constraint.length() == 1 || constraint.length() == 2 && constraint.contains("r")) {
			constraint = constraint.replace("r", "");
			if(constraint.getBytes()[0] <= 57) {
				result = RuleType.COLUMN;
			} else {
				result = RuleType.ROW;
			}
		} else if(constraint.length() > 4) {
			result = RuleType.RECTANGLE;
		}

		return result;
	}

	public String toString() {
		return this.constraint + " " + this.keyword;
	}

	private static int index(int rowSize, int row, int column) {
		return row * rowSize + column;
	}

	private static void reverseArray(int[] data) {
		int left = 0;

		for(int right = data.length - 1; left < right; --right) {
			int temp = data[left];
			data[left] = data[right];
			data[right] = temp;
			++left;
		}

	}

	public int compareTo(Object x0) {
		return this.compareTo((SortingRule)x0);
	}

	public static enum RuleType {
		RECTANGLE(1),
		ROW(2),
		COLUMN(3),
		TILE(4);

		private int lowestPriority;
		private int highestPriority;

		private RuleType(int priorityLevel) {
			this.lowestPriority = priorityLevel * 1000000;
			this.highestPriority = (priorityLevel + 1) * 1000000 - 1;
		}

		public int getLowestPriority() {
			return this.lowestPriority;
		}

		public int getHighestPriority() {
			return this.highestPriority;
		}
	}
}
