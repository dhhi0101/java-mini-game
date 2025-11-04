import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RandomMovingMonsterEx extends JFrame {
	private int count = 0; // 잡은 몬스터의 수
	
	public RandomMovingMonsterEx() {
		setTitle("Kill the Monster!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		Container c = getContentPane(); 
		c.setLayout(null);  // 레이아웃 매니저 사용 X
		
		// 원하는 만큼 몬스터 생성 (번갈아 공격, 방어 몬스터 생성)
		for (int i=0; i<14; i++) {
			Monster la;
			if (i % 2 == 0)
				la = new AttackMonster();
			else
				la = new DefenseMonster();
			
			la.setSize(65,50); // 너비, 높이 설정
			la.setOpaque(true); // 배경색 보이도록 함
			la.addMouseListener(new MyMouseAdapter()); // 마우스 이벤트 리스너
			la.setLocation((int)(Math.random()*450), (int)(Math.random()*450)); // 랜덤한 위치에 몬스터 배치
			c.add(la); 
			new MoveThread(la, c).start();  // 몬스터를 움직이게 하는 스레드
		}
		
		setSize(500,500);  // 창 크기
		setVisible(true);
	}
	
	public static void main(String[] args) {
		new RandomMovingMonsterEx();  // 실행
	}
	
	public interface Clickable { 
		void click();
	}
	
	class MyMouseAdapter extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			Monster lb = (Monster)e.getSource(); // 마우스 클릭 이벤트가 발생한 몬스터를 가져옴
			
			if (lb.isVisible() == true) {
				lb.click();  // 몬스터 체력 감소
				
				if (lb.health <= 0) {
					count++;
					setTitle("Catch " + count + " Monsters!");
					lb.setVisible(false); // 몬스터를 화면에서 제거 
				}
			}
		}
	}
	
	class MoveThread extends Thread {
		private Monster monster; // 움직일 몬스터
		private Container c; // 몬스터가 움직일 컨테이너
		
		public MoveThread(Monster monster, Container c) {
			this.monster = monster;
			this.c = c;
		}
		
		public void run() {
			while (true) {
				int x = monster.getX(); // 몬스터의 현재 x좌표
				int y = monster.getY(); // 몬스터의 현재 y좌표
				
				// 랜덤한 속도로 몬스터 이동
				double angle = Math.random() * 2.0 * Math.PI;  // 0~2파이
				int moveX = (int)(10 * Math.cos(angle)); // x축 방향으로의 속도 (양수: 오른쪽, 음수: 왼쪽)
				int moveY = (int)(10 * Math.sin(angle)); // y축 방향으로의 속도 (양수: 아래쪽, 음수: 위쪽)
				x += moveX;
				y += moveY;
				
				// 몬스터가 벽에 부딪힐 때 벗어나지 않도록 설정
				if (x < 0) x += 15; // 왼쪽 벽 부딪힐 때
				if (y < 0) y += 15; // 위쪽 벽 부딪힐 때 
				if (x > c.getWidth() - monster.getWidth()) x -= 15; // 오른쪽 벽 부딪힐 때
				if (y > c.getHeight() - monster.getHeight()) y -= 15; // 아래쪽 벽 부딪힐 때
				
				
				monster.setLocation(x, y);
				
				// 일정 시간 대기 (150 밀리초)
				try {
					Thread.sleep(150);
				} catch (InterruptedException e) { // 스레드가 중지 상태일 때 다른 스레드가 이를 중단시키려 할 때 발생하는 예외
					return;
				}
			}
		}
	}
	
	// 몬스터 추상 클래스 생성
	abstract class Monster extends JComponent {
		protected int health;
		protected int attackPower;
		protected int defensePower;
		protected JLabel healthLabel;
		protected JLabel nameLabel;
		
		public Monster() {
			this.health = 100;
			this.attackPower = 10;
			this.defensePower = 7;
			
			this.healthLabel = new JLabel("체력: " + String.valueOf(health)); // 현재 체력을 표시하는 레이블
			this.healthLabel.setForeground(Color.WHITE); // 글꼴 색상
			this.healthLabel.setBounds(10, 0, 60, 50); // 크기와 위치
			this.add(healthLabel); // 레이블을 몬스터에 추가
		}
		
		public void decreaseHealth(int amount) {
			this.health -= (amount - this.defensePower); // 방어력에 따라 체력 감소
			if (this.health < 0 ) this.health = 0;  // 체력 0 이하 되면 0으로 설정
			this.healthLabel.setText("체력: " + String.valueOf(health)); // 체력 레이블 업데이트
		}
		
		public void click() {
			decreaseHealth(25);
		}
	}
	
	// 공격 몬스터
	class AttackMonster extends Monster {
		public AttackMonster() {
			setBackground(Color.RED); // 몬스터 색 빨간색
			this.attackPower = 15;
			this.defensePower = 5;	
			
			// 공격 몬스터 표시 
			this.nameLabel = new JLabel("공격");
			this.nameLabel.setForeground(Color.YELLOW);
			this.nameLabel.setBounds(20,1,25,15);
			this.add(nameLabel);
		}
		
		// 그래픽 생성
		// JComponent를 직접 상속 받았기 때문에 배경색을 그리기 위해서는 paintComponent 메소드를 오버라이드해야 함
		@Override
		protected void paintComponent(Graphics g) {
	         super.paintComponent(g);  // 부모 클래스의 그래픽 출력 메소드 호출 (기본적인 그래픽 설정 수행: 컴포넌트의 배경색으로 전체를 채움)
	         g.setColor(getBackground());  // 배경색으로 그래픽 색상 설정
	         g.fillRect(0, 0, getWidth(), getHeight());  // 그래픽 컴포넌트에 직사각형 그리기
	    }
	}
	
	// 방어 몬스터
	class DefenseMonster extends Monster {
		public DefenseMonster() {
			setBackground(Color.BLUE); // 몬스터 색 파란색
			this.defensePower = 10;
			
			// 방어 몬스터 표시
			this.nameLabel = new JLabel("방어");
			this.nameLabel.setForeground(Color.GREEN);
			this.nameLabel.setBounds(20,1,25,15);
			this.add(nameLabel);
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight()); // 그래픽 컴포넌트에 직사각형 그리기
		}
	}
}