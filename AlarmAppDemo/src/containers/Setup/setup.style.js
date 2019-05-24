import styled from 'styled-components';
// import { media } from '../../utils';

export const SetupWrapper = styled.section`
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

export const SetupCard = styled.div`
  background-color: #fff;
  margin: 30px auto;

  //Overriding the style guide card flexbox settings
  max-width: 50% !important;
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