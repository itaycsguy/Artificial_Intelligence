package assignment1A;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

/**
 * Rush Hour is a problem that could be done faster using AI techniques
 * Flow: using A* algorithm and 2 heuristic functions we are solving 40 Rush Hour problems
 * @author Itay_Guy
 * Date: 02/04/18
 */
public class RushHour implements Comparator {
	public static int PROBLEMS_COUNTER = 1;
	private Stack<State> _open;
	private Stack<State> _close;
	private long _startTime;
	
	public RushHour(String game) {
		this._open = new Stack<State>();
		State firstState = new State(game);
		firstState.computeF();
		this._open.add(firstState);
		this._close = new Stack<State>();
		this._startTime = System.currentTimeMillis();
	}
	
	@Override
	public int compare(Object o1, Object o2) {
		State s1 = ((State)o1);
		State s2 = ((State)o2);
		if(s1.getF() < s2.getF()) return -1;
		if(s1.getF() > s2.getF()) return 1;
		return 0; // else equals
	}
	
	/**
	 * Target: keep the app-flow running and developing new nodes
	 */
	public void solve() {
		boolean isSolutionFound = false;
		while(!this._open.isEmpty()) {
			State minF = Collections.min(this._open,this);
			//System.out.println(minF);
			//System.out.println(minF.getH());
			
			this._close.add(new State(minF));
			this._open.remove(minF);
			
			
			if(minF.isSuccess()) {
				System.out.print("solution for problem number " + (PROBLEMS_COUNTER++) + ": ");
				//System.out.print("solution for problem number " + (PROBLEMS_COUNTER++) + ": ");
				isSolutionFound = true;
				State ptr = minF;
				int solLength = 0;
				while(ptr != null) {
					//System.out.println(ptr);
					System.out.print(ptr.printSingleMovement() + " ");
					ptr = ptr.getPredecessor();
					solLength++;
					//System.out.println();
				}
				System.out.print("=> length of " + solLength  + " movements => took: " + (new DecimalFormat("###.####").format(0.001*(System.currentTimeMillis() - this._startTime))) + "sec");
				System.out.println();
				break;
			}
			
			ArrayList<State> expandedKids = minF.expand();
			
			for(State kid : expandedKids) {
				//System.out.println(kid);
				State affineOpenState = kid.getAffineState(this._open);
				State affineCloseState = kid.getAffineState(this._close);
				if(affineOpenState == null && affineCloseState == null) {
					kid.putPredecessorLink(minF);
					this._open.add(new State(kid));
				} else {
					if(affineOpenState != null) { 
						if(kid.getF() < affineOpenState.getF()) {
							affineOpenState.setF(kid.getF());
							affineOpenState.putPredecessorLink(minF);
						}	
					}
					if(affineCloseState != null) {
						if(kid.getF() < affineCloseState.getF()) {
							affineCloseState.setF(kid.getF());
							affineCloseState.putPredecessorLink(minF);
						}
						if(affineOpenState != null) {
							if(affineOpenState.getF() > affineCloseState.getF()) {
								this._open.remove(affineOpenState);
							} else {
								this._open.remove(affineCloseState);
							}
						}
					}
				}
			}
			//System.out.println("count of open: " + this._open.size());
			//System.out.println("count of close: " + this._close.size());
		}
		if(!isSolutionFound) {
			System.out.println("no solution is avaliable for problem number " + (PROBLEMS_COUNTER++));
		}
	}

	public static void main(String[] args) {
		System.out.println("Starting Rush-Hour AI Solver...");
		new RushHour("AA...OP..Q.OPXXQ.OP..Q..B...CCB.RRR.").solve();
		new RushHour("A..OOOA..B.PXX.BCPQQQ.CP..D.EEFFDGG.").solve();
		new RushHour(".............XXO...AAO.P.B.O.P.BCC.P").solve();
		new RushHour("O..P..O..P..OXXP....AQQQ..A..B..RRRB").solve();
		new RushHour("AA.O.BP..OQBPXXOQGPRRRQGD...EED...FF").solve();
		new RushHour("AA.B..CC.BOP.XXQOPDDEQOPF.EQ..F..RRR").solve();
		new RushHour(".ABBCD.A.ECD.XXE.F..II.F...H.....H..").solve();
		new RushHour("...AAO..BBCOXXDECOFFDEGGHHIPPPKKIQQQ").solve();
		new RushHour(".ABBCC.A.DEEXX.DOFPQQQOFP.G.OHP.G..H").solve();
		new RushHour("AAB.CCDDB..OPXX..OPQQQ.OP..EFFGG.EHH").solve();
		new RushHour("OAAP..O..P..OXXP....BQQQ..B..E..RRRE").solve();
		new RushHour("ABB..OA.P..OXXP..O..PQQQ....C.RRR.C.").solve();
		new RushHour("AABBC...D.CO.EDXXOPE.FFOP..GHHPIIGKK").solve();
		new RushHour("AAB.....B.CCDEXXFGDEHHFG..I.JJKKI...").solve();
		new RushHour(".AABB.CCDDOPQRXXOPQREFOPQREFGG.HHII.").solve();
		new RushHour("AABBCOD.EECODFPXXO.FPQQQ..P...GG....").solve();
		new RushHour("AOOO..A.BBCCXXD...EEDP..QQQPFGRRRPFG").solve();
		new RushHour("AABO..CCBO..PXXO..PQQQ..PDD...RRR...").solve();
		new RushHour("..ABB...A.J..DXXJ..DEEF..OOOF.......").solve();
		new RushHour("A..OOOABBC..XXDC.P..D..P..EFFP..EQQQ").solve();
		new RushHour("AABO..P.BO..PXXO..PQQQ...........RRR").solve();
		new RushHour("..AOOOB.APCCBXXP...D.PEEFDGG.HFQQQ.H").solve();
		new RushHour("..OOOP..ABBP..AXXP..CDEE..CDFF..QQQ.").solve();
		new RushHour("..ABB..CA...DCXXE.DFF.E.OOO.G.HH..G.").solve();
		new RushHour("AAB.CCDDB..OPXX.EOPQQQEOPF.GHH.F.GII").solve();
		new RushHour(".A.OOOBA.CP.BXXCPDERRRPDE.F..G..FHHG").solve();
		new RushHour("ABBO..ACCO..XXDO.P..DEEP..F..P..FRRR").solve();
		new RushHour("OOOA....PABBXXP...CDPEEQCDRRRQFFGG.Q").solve();
		new RushHour("OOO.P...A.P.XXA.PBCDDEEBCFFG.HRRRG.H").solve();
		new RushHour("O.APPPO.AB..OXXB..CCDD.Q.....QEEFF.Q").solve();
		new RushHour("AA.OOO...BCCDXXB.PD.QEEPFFQ..P..QRRR").solve();
		new RushHour("AAOBCC..OB..XXO...DEEFFPD..K.PHH.K.P").solve();
		new RushHour(".AR.BB.AR...XXR...IDDEEPIFFGHPQQQGHP").solve();
		new RushHour("A..RRRA..B.PXX.BCPQQQDCP..EDFFIIEHH.").solve();
		new RushHour("..OAAP..OB.PXXOB.PKQQQ..KDDEF.GG.EF.").solve();
		new RushHour("OPPPAAOBCC.QOBXX.QRRRD.Q..EDFFGGE...").solve();
		new RushHour("AAB.CCDDB.OPQXX.OPQRRROPQ..EFFGG.EHH").solve();
		new RushHour("A..OOOABBC..XXDC.R..DEER..FGGR..FQQQ").solve();
		new RushHour("..AOOO..AB..XXCB.RDDCEERFGHH.RFGII..").solve();
		new RushHour("OAA.B.OCD.BPOCDXXPQQQE.P..FEGGHHFII.").solve();
		System.out.println("Game Over!");
	}
}
