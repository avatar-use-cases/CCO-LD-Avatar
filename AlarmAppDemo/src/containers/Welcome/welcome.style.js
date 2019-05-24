import styled from 'styled-components';

export const WelcomeWrapper = styled.section`
  width: 100%;
  background-image: url('/img/texture.png');
  background-repeat: repeat;
  padding: 50px 0;

  h3 {
    color: #666666;
    span {
      font-weight: bold;
    }
    a {
      font-size: 1.9rem;
    }
  }
`;

export const WelcomeCard = styled.div`
  background-color: #fff;
  margin: 30px auto;

  //Overriding the style guide card flexbox settings
  max-width: 80% !important;
  // flex-direction: row !important;
  padding: 50px 0 !important; //temporary fix to a style guide bug

  align-items: center;
  align-content: center;
  textAlign:center;
  a {
    text-decoration: none;
    &:hover {
      text-decoration: underline;
    }
  }

  // button {
  //   margin-left: 8px;
  // }

`;


export const ImageWrapper = styled.div`
display: flex;
justify-content: center;
align-items: center;

button {
  margin-left: 0px;
}
`

export const WelcomeDetail = styled.div`
  padding: 1rem 3.5rem;

  p,
  li {
    color: #666666;
  }
  ul {
    list-style: disc;
    margin: 0 18px;
  }
`;
